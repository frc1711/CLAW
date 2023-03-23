package claw.hardware.swerve;

import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.constraint.SwerveDriveKinematicsConstraint;

/**
 * A class representing acceleration and velocity motion constraints on a {@link SwerveDriveHandler},
 * which provides useful functionality for limiting acceleration both in real time and in {@link Trajectory}
 * calculations.
 */
public class SwerveMotionConstraints {
    
    public final double maxVelocityMetersPerSec, maxAccelerationPerSecSq;
    
    /**
     * Create new {@link SwerveMotionConstraints} given a maximum acceleration and velocity.
     * @param maxVelocityMetersPerSec   The maximum allowed velocity of the robot, in meters per second.
     * @param maxAccelerationPerSecSq   The maximum allowed acceleration of the robot, in meters per second squared.
     */
    public SwerveMotionConstraints (
        double maxVelocityMetersPerSec,
        double maxAccelerationPerSecSq
    ) {
        this.maxVelocityMetersPerSec = maxVelocityMetersPerSec;
        this.maxAccelerationPerSecSq = maxAccelerationPerSecSq;
    }
    
    /**
     * Get a {@link TrajectoryConfig} which limits the swerve drive to the provided acceleration and velocity limits,
     * along with special constraint(s) to account for swerve drive kinematics.
     * @param swerveDrive   The {@link SwerveDriveHandler} to use for kinematics information.
     * @return              A {@code TrajectoryConfig} for limiting the trajectory's acceleration and velocity.
     */
    public TrajectoryConfig getTrajectoryConfig (SwerveDriveHandler swerveDrive) {
        return
            new TrajectoryConfig(
                maxVelocityMetersPerSec,
                maxAccelerationPerSecSq
            ).addConstraint(
                new SwerveDriveKinematicsConstraint(
                    swerveDrive.getKinematics(),
                    swerveDrive.getMaxDriveSpeedMetersPerSec()
                )
            );
    }
    
}
