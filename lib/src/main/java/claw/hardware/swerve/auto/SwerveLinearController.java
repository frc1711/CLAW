package claw.hardware.swerve.auto;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.math.Vector;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.wpilibj.Timer;

/**
 * An swerve drive controller which uses {@link SimpleLinearTrajectory} to drive the robot to a setpoint.
 * Due to constraints of the {@code SimpleLinearTrajectory}, it is best to not change the setpoint at all
 * or change it very little over time.
 */
public class SwerveLinearController {
    
    private final SwerveDriveHandler swerveDrive;
    private final SwerveMotionConstraints constraints;
    
    private final Timer trajectoryTimer = new Timer();
    private final ChassisSpeedsFilter speedsFilter;
    private final PIDController xController, yController, thetaController;
    
    private SimpleLinearTrajectory linearTrajectory;
    private Pose2d lastSetpoint;
    
    /**
     * Constructs a new {@link SwerveLinearController}.
     * @param swerveDrive       The {@link SwerveDriveHandler} this controller will act on.
     * @param constraints       The {@link SwerveMotionConstraints} to impose on the generated trajectories.
     * @param xController       A {@link PIDController} receiving x-offsets from desired trajectory pose in meters
     * as input, and outputting adjustments in swerve drive velocity in meters per second.
     * @param yController       A PID controller receiving y-offsets from desired trajectory pose in meters
     * as input, and outputting adjustments in swerve drive velocity in meters per second.
     * @param thetaController   A PID controller receiving rotational offsets from desired trajectory pose in radians
     * as input, and outputting adjustments in swerve drive angular velocity in radians per second.
     */
    public SwerveLinearController (
        SwerveDriveHandler swerveDrive,
        SwerveMotionConstraints constraints,
        PIDController xController,
        PIDController yController,
        PIDController thetaController
    ) {
        this.swerveDrive = swerveDrive;
        this.constraints = constraints;
        
        this.xController = xController;
        this.yController = yController;
        this.thetaController = thetaController;
        
        speedsFilter = new ChassisSpeedsFilter(swerveDrive, constraints, new ChassisSpeeds());
        resetController();
    }
    
    /**
     * Reset the controller and its setpoint.
     */
    public void resetController () {
        speedsFilter.reset();
        xController.reset();
        yController.reset();
        thetaController.reset();
        resetLinearTrajectory(new Pose2d());
    }
    
    /**
     * Drives the robot to a final setpoint {@link Pose2d}, according to this controller's settings.
     * @param setpoint  The setpoint pose of this controller. This controller is best for cases where
     * the setpoint does not change or changes very little throughout the course of the trajectory. 
     */
    public void driveToSetpoint (Pose2d setpoint) {
        swerveDrive.drive(calculateSpeeds(setpoint));
    }
    
    /**
     * Get the final {@link Pose2d} setpoint of this controlller.
     * @return  The desired final robot pose.
     */
    public Pose2d getSetpoint () {
        return lastSetpoint;
    }
    
    /**
     * Calculates robot-relative chassis speeds given the desired setpoint
     */
    private ChassisSpeeds calculateSpeeds (Pose2d setpoint) {
        
        // Reset the linear trajectory if the setpoint poses do not match
        if (!setpoint.equals(getSetpoint())) {
            resetLinearTrajectory(setpoint);
        }
        
        // Get the trajectory-calculated position and speeds
        SwerveTrajectoryPoint trajectoryPoint = linearTrajectory.getPoint(trajectoryTimer.get());
        Pose2d trajPose = trajectoryPoint.getDesiredPose();
        ChassisSpeeds trajFieldRelSpeeds = trajectoryPoint.getDesiredSpeeds();
        
        // Get offsets from the trajectory-calculated positions and pass through PIDs
        ChassisSpeeds offsetCorrectionSpeeds = getOffsetCorrectionSpeeds(swerveDrive.getRobotPoseEstimate(), trajPose);
        
        // Sum the offset correction speeds with the trajectory speeds to get final desired robot speeds
        ChassisSpeeds desiredRobotRelSpeeds = swerveDrive.toRobotRelativeSpeeds(
            new ChassisSpeeds(
                trajFieldRelSpeeds.vxMetersPerSecond + offsetCorrectionSpeeds.vxMetersPerSecond,
                trajFieldRelSpeeds.vyMetersPerSecond + offsetCorrectionSpeeds.vyMetersPerSecond,
                trajFieldRelSpeeds.omegaRadiansPerSecond + offsetCorrectionSpeeds.omegaRadiansPerSecond
            )
        );
        
        // Filter the desired speeds and return them
        return speedsFilter.calculate(desiredRobotRelSpeeds);
        
    }
    
    /**
     * Pass the offsets from the currentPose to the desired trajectoryPose through the PID loops
     * to get ChassisSpeeds to counteract this difference.
     */
    private ChassisSpeeds getOffsetCorrectionSpeeds (Pose2d currentPose, Pose2d trajectoryPose) {
        
        double xCorrection = xController.calculate(currentPose.getX(), trajectoryPose.getX());
        double yCorrection = yController.calculate(currentPose.getY(), trajectoryPose.getY());
        
        double thetaSetpoint = MathUtil.inputModulus(
            trajectoryPose.getRotation().minus(currentPose.getRotation()).getRadians(),
            -Math.PI, Math.PI
        );
        
        double thetaCorrection = thetaController.calculate(0, thetaSetpoint);
        
        return new ChassisSpeeds(xCorrection, yCorrection, thetaCorrection);
        
    }
    
    /**
     * Resets the controller's setpoint - Reset the linear trajectory so the robot can drive
     * to the new final pose. Initial velocities for this new trajectory will be based on the last
     * chassis speeds commanded to the robot.
     */
    private void resetLinearTrajectory (Pose2d newFinalPose) {
        
        // Set the last setpoint field and reset the timer
        lastSetpoint = newFinalPose;
        trajectoryTimer.reset();
        trajectoryTimer.start();
        
        // Get pose and last commanded speeds
        Pose2d currentPose = swerveDrive.getRobotPoseEstimate();
        
        var currentDriveVelocityVector = Vector.from(
            speedsFilter.getLastSpeeds().vxMetersPerSecond,
            speedsFilter.getLastSpeeds().vyMetersPerSecond
        );
        
        double currentAngularVelocity = speedsFilter.getLastSpeeds().omegaRadiansPerSecond;
        
        // Calculate drive linear velocity in the direction of the new finalPose. This isn't going to be perfect because
        // almost certainly swerve isn't driving exactly in the direction of the new finalPose, but if the final pose
        // changes slowly or not very much, it should be OK
        Translation2d translationToFinalPose = newFinalPose.getTranslation().minus(currentPose.getTranslation());
        Vector<N2> unitVectorInDirOfNewTrajectory = Vector.from(translationToFinalPose.getAngle(), 1);
        
        // The dot product calculates the signed length of the projection of the current drive velocity vector
        // onto the new trajectory line, so this is our new initial linear velocity
        double currentLinearVelocityInNewDir = currentDriveVelocityVector.dotProduct(unitVectorInDirOfNewTrajectory);
        
        // Create a new trajectory based on the initial velocities
        linearTrajectory = new SimpleLinearTrajectory(
            currentPose, newFinalPose,
            currentLinearVelocityInNewDir,
            currentAngularVelocity,
            0, 0,
            constraints
        );
        
    }
    
}
