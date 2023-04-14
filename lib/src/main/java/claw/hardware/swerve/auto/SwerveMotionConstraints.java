package claw.hardware.swerve.auto;

import claw.hardware.swerve.SwerveDriveHandler;
import edu.wpi.first.math.trajectory.Trajectory;

/**
 * A class representing acceleration and velocity motion constraints on a {@link SwerveDriveHandler},
 * which provides useful functionality for limiting acceleration both in real time and in {@link Trajectory}
 * calculations.
 */
public class SwerveMotionConstraints {
    
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
        double maxDriveSpeed,
        double maxDriveAcceleration,
        double maxRotationSpeed,
        double maxRotationAcceleration
    ) {
        this.maxDriveSpeed =            maxDriveSpeed;
        this.maxDriveAcceleration =     maxDriveAcceleration;
        this.maxRotationSpeed =         maxRotationSpeed;
        this.maxRotationAcceleration =  maxRotationAcceleration;
    }
    
}
