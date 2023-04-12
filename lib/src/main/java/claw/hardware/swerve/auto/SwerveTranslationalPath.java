package claw.hardware.swerve.auto;

import java.util.List;

import claw.math.LinearInterpolator;
import claw.math.Transform;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;

public class SwerveTranslationalPath {
    
    private final Trajectory translationalTrajectory;
    private final Transform sampleTimeToRobotRotationRadians;
    private final double totalTimeSecs;
    
    public SwerveTranslationalPath (Pose2d firstPose, List<Translation2d> intermediateWaypoints, Pose2d lastPose, TrajectoryConfig config) {
        
        // Generate the translational portion of the trajectory
        translationalTrajectory = generateSafeTrajectory(firstPose, intermediateWaypoints, lastPose, config);
        
        // Total trajectory time in seconds
        totalTimeSecs = translationalTrajectory.getTotalTimeSeconds();
        
        // Transform from sample time to a linear interpolation from the start to end time
        final double
            startRotationRadians = firstPose.getRotation().getRadians(),
            endRotationRadians = lastPose.getRotation().getRadians();
        
        sampleTimeToRobotRotationRadians =
            Transform.clamp(0, totalTimeSecs)
            .then(new LinearInterpolator(
                0,              startRotationRadians,
                totalTimeSecs,  endRotationRadians
            ));
        
    }
    
    public Rotation2d getDesiredRotation (double time) {
        return new Rotation2d(sampleTimeToRobotRotationRadians.apply(time));
    }
    
    public Trajectory.State getDesiredState (double time) {
        return translationalTrajectory.sample(time);
    }
    
    public ChassisSpeeds driveController (double time, Pose2d currentPose, HolonomicDriveController controller) {
        Rotation2d desiredRotation = getDesiredRotation(time);
        Trajectory.State desiredState = getDesiredState(time);
        return controller.calculate(currentPose, desiredState, desiredRotation);
    }
    
    /**
     * Get the field-relative rotation which describes the direction in which the robot must drive to move from
     * the {@code initialPose} to the {@code destinationPose}
     */
    private static Rotation2d getDriveDirection (Translation2d initialPose, Translation2d destinationPose) {
        // Get the translational movement from the initial pose to the destination
        Translation2d translationalMovement = destinationPose.minus(initialPose);
        
        // Get the direction the translational movement corresponds with
        return translationalMovement.getAngle();
    }
    
    /**
     * Generates a trajectory which describes the desired translational movement correctly (but not robot rotation).
     * This is necessary because the trajectory generation requires that the robot move in the direction it is facing in,
     * but for holonomic drivetrains (like swerve drive) this isn't necessary, and can prevent generating valid trajectories
     * which involve driving in directions other than forward.
     */
    private static Trajectory generateSafeTrajectory (
        Pose2d firstPose,
        List<Translation2d> waypoints,
        Pose2d lastPose,
        TrajectoryConfig config
    ) {
        
        final Translation2d firstTranslation = firstPose.getTranslation(), lastTranslation = lastPose.getTranslation();
        
        // Adjust the first and last poses so they're driving in the direction of nearby waypoints
        // (Helps to mitigate MalformedSplineExceptions)
        Pose2d adjustedFirstPose, adjustedLastPose;
        
        // Adjust depending on the presence of intermediate waypoints
        if (waypoints.size() == 0) {
            
            // Adjust for drive rotation from first pose to last pose
            Rotation2d driveRotation = getDriveDirection(firstTranslation, lastTranslation);
            adjustedFirstPose = new Pose2d(firstTranslation, driveRotation);
            adjustedLastPose = new Pose2d(lastTranslation, driveRotation);
            
        } else {
            
            // Adjust for drive rotation from first pose to first waypoint, and last waypoint to last pose
            adjustedFirstPose = new Pose2d(
                firstTranslation,
                getDriveDirection(
                    firstTranslation,
                    waypoints.get(0)
                )
            );
            
            adjustedLastPose = new Pose2d(
                lastTranslation,
                getDriveDirection(
                    waypoints.get(waypoints.size() - 1),
                    lastTranslation
                )
            );
            
        }
        
        // Generate the trajectory from the adjusted first and last poses, plus waypoints
        return TrajectoryGenerator.generateTrajectory(
            adjustedFirstPose,
            waypoints,
            adjustedLastPose,
            config
        );
        
    }
    
}
