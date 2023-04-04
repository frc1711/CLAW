package claw.hardware.swerve.auto;

import claw.hardware.swerve.SwerveDriveHandler;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.constraint.SwerveDriveKinematicsConstraint;

/**
 * A class representing acceleration and velocity motion constraints on a {@link SwerveDriveHandler},
 * which provides useful functionality for limiting acceleration both in real time and in {@link Trajectory}
 * calculations.
 */
public class SwerveMotionConstraints {
    
    private final SwerveDriveHandler swerveDrive;
    
    public final double
        maxDriveSpeed,
        maxDriveAcceleration,
        maxRotationSpeed,
        maxRotationAcceleration;
    
    /**
     * Create new {@link SwerveMotionConstraints} given a maximum acceleration and velocity.
     * @param maxDriveSpeed             The maximum allowable drive speed, measured in meters per second.
     * @param maxDriveAcceleration      The maximum allowable drive acceleration, measured in meters per
     * second squared.
     * @param maxRotationSpeed          The maximum allowable rotation speed, measured in radians per second.
     * @param maxRotationAcceleration   The maximum allowable rotational acceleration, measured in radians
     * per second squared.
     */
    public SwerveMotionConstraints (
        SwerveDriveHandler swerveDrive,
        double maxDriveSpeed,
        double maxDriveAcceleration,
        double maxRotationSpeed,
        double maxRotationAcceleration
    ) {
        this.swerveDrive = swerveDrive;
        this.maxDriveSpeed =            maxDriveSpeed;
        this.maxDriveAcceleration =     maxDriveAcceleration;
        this.maxRotationSpeed =         maxRotationSpeed;
        this.maxRotationAcceleration =  maxRotationAcceleration;
    }
    
    /**
     * Get a {@link TrajectoryConfig} which limits the swerve drive to the provided acceleration and velocity limits,
     * along with special constraint(s) to account for swerve drive kinematics.
     * @return              A {@code TrajectoryConfig} for limiting the trajectory's acceleration and velocity.
     */
    public TrajectoryConfig getTrajectoryConfig () {
        
        // TODO: Apply rotational limits
        
        return
            new TrajectoryConfig(
                maxDriveSpeed,
                maxDriveAcceleration
            ).addConstraint(
                // SwerveDriveKinematicsConstraint restricts the kinematics assuming that
                // the swerve drive is actually turning according to the trajectory, even
                // though this very well may not be the case, especially if we want to be able
                // to use a trajectory that involves driving in a different direction than
                // the robot is rotated in
                new SwerveDriveKinematicsConstraint(
                    swerveDrive.getKinematics(),
                    swerveDrive.getMaxDriveSpeedMetersPerSec()
                )
            );
    }
    
}
