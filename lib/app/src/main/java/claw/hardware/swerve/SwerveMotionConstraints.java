package claw.hardware.swerve;

import claw.math.Vector;
import claw.math.VectorVelocityLimiter;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N2;
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
                new SwerveDriveKinematicsConstraint(
                    swerveDrive.getKinematics(),
                    swerveDrive.getMaxDriveSpeedMetersPerSec()
                )
            );
    }
    
    /**
     * Get a new {@link ChassisSpeedsFilter} which filters input {@link ChassisSpeeds} according
     * to these motion constraints, allowing for real-time acceleration and velocity limiting.
     * @param initialSpeeds The current robot-relative speeds.
     * @return              The speeds filter for these motion constraints.
     */
    public ChassisSpeedsFilter getSpeedsFilter (ChassisSpeeds initialSpeeds) {
        return new ChassisSpeedsFilter(initialSpeeds);
    }
    
    /**
     * Get a new {@link ChassisSpeedsFilter} which filters input {@link ChassisSpeeds} according
     * to these motion constraints, allowing for real-time acceleration and velocity limiting.
     * This assumes that the current robot speeds are zero.
     * @return              The speeds filter for these motion constraints.
     */
    public ChassisSpeedsFilter getSpeedsFilter () {
        return getSpeedsFilter(new ChassisSpeeds());
    }
    
    /**
     * Filters desired {@link ChassisSpeeds} according to {@link SwerveMotionConstraints}, allowing
     * for real-time acceleration and velocity limiting.
     */
    public class ChassisSpeedsFilter {
        
        private final SlewRateLimiter rotationAccelerationLimiter;
        
        /**
         * The "velocity" of some velocity vector is acceleration. This limits
         * the acceleration, where the input to the driveAccelerationLimiter
         * is field-relative velocity.
         */
        private final VectorVelocityLimiter<N2> driveAccelerationLimiter;
        
        private ChassisSpeedsFilter (ChassisSpeeds initialSpeeds) {
            
            // Acceleration constraints
            rotationAccelerationLimiter = new SlewRateLimiter(maxRotationAcceleration);
            driveAccelerationLimiter = new VectorVelocityLimiter<>(
                Vector.from(0, 0),
                maxDriveAcceleration
            );
            
            // Reset to the initial speeds
            reset(initialSpeeds);
            
        }
        
        /**
         * Reset the {@link ChassisSpeedsFilter} to the given {@link ChassisSpeeds}.
         * @param robotRelSpeeds The current robot-relative speeds to reset to.
         */
        public void reset (ChassisSpeeds robotRelSpeeds) {
            rotationAccelerationLimiter.reset(robotRelSpeeds.omegaRadiansPerSecond);
            driveAccelerationLimiter.reset(
                getFieldRelVelocityFromRobotRelSpeeds(robotRelSpeeds)
            );
        }
        
        /**
         * Calculate output {@link ChassisSpeeds} from the desired speeds, according to the
         * {@link SwerveMotionConstraints} (limiting velocity, acceleration, etc.).
         * @param desiredRobotRelSpeeds The desired robot-relative speeds.
         * @return                      The given speeds, adjusted to fit the motion constraints.
         */
        public ChassisSpeeds calculate (ChassisSpeeds desiredRobotRelSpeeds) {
            
            // Calculate output rotation speed
            double outputRotationSpeed = calculateRotationSpeed(desiredRobotRelSpeeds.omegaRadiansPerSecond);
            
            // Calculate output field-relative drive velocity
            Vector<N2> outputFieldRelVelocity = calculateDriveVelocity(
                getFieldRelVelocityFromRobotRelSpeeds(desiredRobotRelSpeeds)
            );
            
            // Convert rotation speed and field relative velocity into robot relative speeds
            return getRobotRelSpeedsFromFieldRelVelocity(
                outputFieldRelVelocity,
                outputRotationSpeed
            );
            
        }
        
        /**
         * Apply the acceleration and max speed filters to the rotation speed
         */
        private double calculateRotationSpeed (double desiredRotationalSpeed) {
            return rotationAccelerationLimiter.calculate(
                MathUtil.clamp(desiredRotationalSpeed, -maxRotationSpeed, maxRotationSpeed)
            );
        }
        
        /**
         * Apply the acceleration and max velocity filters to the drive velocity
         * @param desiredDriveVelocity Must be field-relative
         */
        private Vector<N2> calculateDriveVelocity (Vector<N2> desiredFieldRelVelocity) {
            // Apply the acceleration limiter
            return driveAccelerationLimiter.calculate(
                // Scale the desired drive velocity to a maximum magnitude of maxDriveSpeed
                desiredFieldRelVelocity.getMagnitude() > maxDriveSpeed
                    ? desiredFieldRelVelocity.scaleToMagnitude(maxDriveSpeed)
                    : desiredFieldRelVelocity
            );
        }
        
        /**
         * Convert robot-relative ChassisSpeeds into field-relative velocity vector
         * (undoes {@link #getRobotRelSpeedsFromFieldRelVelocity(Vector, double)})
         */
        private Vector<N2> getFieldRelVelocityFromRobotRelSpeeds (ChassisSpeeds robotRelSpeeds) {
            return Vector.rotateBy(
                Vector.from(
                    robotRelSpeeds.vxMetersPerSecond,
                    robotRelSpeeds.vyMetersPerSecond
                ),
                swerveDrive.getAbsoluteRobotRotation()
            );
        }
        
        /**
         * Convert field-relative velocity vector into robot-relative ChassisSpeeds
         * (undoes {@link #getFieldRelVelocityFromRobotRelSpeeds(ChassisSpeeds)})
         */
        private ChassisSpeeds getRobotRelSpeedsFromFieldRelVelocity (Vector<N2> fieldRelVelocity, double rotationSpeed) {
            Vector<N2> robotRelVelocity = Vector.rotateBy(
                fieldRelVelocity,
                swerveDrive.getAbsoluteRobotRotation().unaryMinus()
            );
            
            return new ChassisSpeeds(
                robotRelVelocity.getX(),
                robotRelVelocity.getY(),
                rotationSpeed
            );
        }
        
    }
    
}
