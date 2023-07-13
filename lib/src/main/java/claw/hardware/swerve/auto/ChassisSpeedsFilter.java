package claw.hardware.swerve.auto;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.math.Vector;
import claw.math.VectorVelocityLimiter;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N2;

/**
 * Filters desired {@link ChassisSpeeds} according to {@link SwerveMotionConstraints}, allowing
 * for real-time acceleration and velocity limiting.
 */
public class ChassisSpeedsFilter {
    
    private final SwerveDriveHandler swerveDrive;
    private final SwerveMotionConstraints constraints;
    private final SlewRateLimiter rotationAccelerationLimiter;
    
    /**
     * The "velocity" of some velocity vector is acceleration. This limits
     * the acceleration, where the input to the driveAccelerationLimiter
     * is field-relative velocity.
     */
    private final VectorVelocityLimiter<N2> driveAccelerationLimiter;
    
    private ChassisSpeeds lastCommandedSpeeds;
    
    /**
     * Create a new {@link ChassisSpeedsFilter} for a particular swerve drive with a set of motion constraints.
     * @param swerveDrive   The {@link SwerveDriveHandler} on which the chassis speeds are filtered.
     * @param constraints   The {@link SwerveMotionConstraints} describing maximum accelerations, velocities, etc.
     * @param initialSpeeds The initial {@link ChassisSpeeds} to use for this speeds filter.
     */
    public ChassisSpeedsFilter (SwerveDriveHandler swerveDrive, SwerveMotionConstraints constraints, ChassisSpeeds initialSpeeds) {
        
        this.swerveDrive = swerveDrive;
        this.constraints = constraints;
        
        // Acceleration constraints
        rotationAccelerationLimiter = new SlewRateLimiter(constraints.maxRotationAcceleration);
        driveAccelerationLimiter = new VectorVelocityLimiter<>(
            Vector.from(0, 0),
            constraints.maxDriveAcceleration
        );
        
        // Reset to the initial speeds
        reset(initialSpeeds);
        
    }
    
    /**
     * Reset the {@link ChassisSpeedsFilter} to zero movement {@link ChassisSpeeds}.
     */
    public void reset () {
        reset(new ChassisSpeeds());
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
        
        lastCommandedSpeeds = robotRelSpeeds;
    }
    
    /**
     * Calculate output {@link ChassisSpeeds} from the desired speeds, according to the
     * {@link SwerveMotionConstraints} (limiting velocity, acceleration, etc.). <b>This input must be robot-relative</b>.
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
        lastCommandedSpeeds = getRobotRelSpeedsFromFieldRelVelocity(
            outputFieldRelVelocity,
            outputRotationSpeed
        );
        
        return lastCommandedSpeeds;
    }
    
    /**
     * Get the last {@link ChassisSpeeds} outputted by the filter, or whatever the filter was reset to.
     * @return  The last speeds outputted by the filter.
     */
    public ChassisSpeeds getLastSpeeds () {
        return lastCommandedSpeeds;
    }
    
    /**
     * Apply the acceleration and max speed filters to the rotation speed
     */
    private double calculateRotationSpeed (double desiredRotationalSpeed) {
        return rotationAccelerationLimiter.calculate(
            MathUtil.clamp(desiredRotationalSpeed, -constraints.maxRotationSpeed, constraints.maxRotationSpeed)
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
            desiredFieldRelVelocity.getMagnitude() > constraints.maxDriveSpeed
                ? desiredFieldRelVelocity.scaleToMagnitude(constraints.maxDriveSpeed)
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
            swerveDrive.getRobotRotation()
        );
    }
    
    /**
     * Convert field-relative velocity vector into robot-relative ChassisSpeeds
     * (undoes {@link #getFieldRelVelocityFromRobotRelSpeeds(ChassisSpeeds)})
     */
    private ChassisSpeeds getRobotRelSpeedsFromFieldRelVelocity (Vector<N2> fieldRelVelocity, double rotationSpeed) {
        Vector<N2> robotRelVelocity = Vector.rotateBy(
            fieldRelVelocity,
            swerveDrive.getRobotRotation().unaryMinus()
        );
        
        return new ChassisSpeeds(
            robotRelVelocity.getX(),
            robotRelVelocity.getY(),
            rotationSpeed
        );
    }
    
}
