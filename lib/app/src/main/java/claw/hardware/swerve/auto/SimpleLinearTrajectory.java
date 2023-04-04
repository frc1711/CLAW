package claw.hardware.swerve.auto;

import claw.math.Vector;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.trajectory.TrapezoidProfile;

/**
 * A simple trajectory using {@link SwerveMotionConstraints} to drive the robot from some zero-velocity
 * initial pose to a zero-velocity final pose.
 */
public class SimpleLinearTrajectory implements SwerveTrajectory {
    
    private final MotionProfileWrapper driveProfile, rotationProfile;
    
    private final Vector<N2> driveInitialOffset;
    private final Rotation2d driveDirection;
    private final double rotationInitialOffset;
    
    private final double totalDuration;
    
    /**
     * Creates a new {@link SimpleLinearTrajectory}, driving the robot from some zero-velocity {@code initialPose}
     * to some zero-velocity {@code finalPose}.
     * @param initialPose   The initial {@link Pose2d} of the robot when the trajectory is beginning.
     * @param finalPose     The final pose of the robot as the trajectory ends.
     * @param constraints   The {@link SwerveMotionConstraints} to impose on the trajectory.
     */
    public SimpleLinearTrajectory (Pose2d initialPose, Pose2d finalPose, SwerveMotionConstraints constraints) {
        
        // Get the total translation driven between the initial and final poses
        Translation2d totalDriveTranslation = finalPose.getTranslation().minus(initialPose.getTranslation());
        
        // A translational TrapezoidProfile along the diagonal from initialPose to finalPose
        var driveProfileUnadjusted = new TrapezoidProfile(
            new TrapezoidProfile.Constraints(constraints.maxDriveSpeed, constraints.maxDriveAcceleration),
            
            // State describes the distance along the diagonal translation so we can apply maxDriveSpeed
            // and maxDriveAcceleration to the actual euclidean distances driven
            new TrapezoidProfile.State(totalDriveTranslation.getNorm(), 0)
        );
        
        // Set the drive offset and direction
        driveInitialOffset = Vector.from(initialPose.getX(), initialPose.getY());
        driveDirection = totalDriveTranslation.getAngle();
        
        // Optimize the radians to travel in the heading
        double radiansToTravel = MathUtil.inputModulus(
            finalPose.getRotation().minus(initialPose.getRotation()).getRadians(),
            -Math.PI, Math.PI
        );
        
        // A rotational TrapezoidProfile from the initialPose's rotation to finalPose
        var rotationProfileUnadjusted = new TrapezoidProfile(
            new TrapezoidProfile.Constraints(constraints.maxRotationSpeed, constraints.maxRotationAcceleration),
            new TrapezoidProfile.State(radiansToTravel, 0)
        );
        
        // Set the rotation offset
        rotationInitialOffset = initialPose.getRotation().getRadians();
        
        // Set the drive and rotation profile, adjusting so they take the same amount of time
        driveProfile = scaleToLongerProfile(driveProfileUnadjusted, rotationProfileUnadjusted);
        rotationProfile = scaleToLongerProfile(rotationProfileUnadjusted, driveProfileUnadjusted);
        totalDuration = Math.max(driveProfileUnadjusted.totalTime(), rotationProfileUnadjusted.totalTime());
        
    }
    
    /**
     * Scales the given motion profile so that it takes the same amount of time as {@code other} if
     * it currently takes less time than {@code other}. Otherwise, does nothing to the given profile to scale.
     * 
     * This is useful for scaling two concurrent trapezoid profiles so they end up taking the same amount of time.
     * 
     * Note: This can only scale properly if the initial and final velocities of the profile are zero
     */
    private static MotionProfileWrapper scaleToLongerProfile (TrapezoidProfile profileToScale, TrapezoidProfile other) {
        
        if (profileToScale.totalTime() >= other.totalTime()) {
            return MotionProfileWrapper.fromTrapezoidProfile(profileToScale);
        }
        
        return scaleToTotalTime(profileToScale, other.totalTime());
        
    }
    
    /**
     * Note: This can only scale properly if the initial and final velocities of the profile are zero
     */
    private static MotionProfileWrapper scaleToTotalTime (TrapezoidProfile profile, double totalTime) {
        
        // If the total time of the scaled version if 0, then simply return the end state of the profile
        if (totalTime == 0) {
            var endState = profile.calculate(profile.totalTime());
            return t -> endState;
        }
        
        /**
         * a is the given profile with duration Da, position Pa, and velocity Va
         * b is the adjusted profile with duration Db, position Pb, and velocity Vb
         * 
         * Because the adjusted profile must start and end with the same positions, let
         * Pb(t) = Pa(Da/Db * t)
         * So, Pb'(t) = (Da/Db) * Pa'(Da/Db * t), where Pb'(t) = Vb(t) and Pa'(t) = Va(t).
         * Thus, Vb(t) = (Da/Db) * Va(Da/Db * t). Letting Da/Db = K, Pb(t) = Pa(K*t), Vb(t) = K*Va(K*t).
         * This will lead to different ending velocities to the two motion profiles if they are nonzero,
         * but all motion profiles we use for the SimpleLinearTrajectory start and end with zero velocity.
         */
        
        double K = profile.totalTime() / totalTime;
        return t -> {
            
            // Get the original profile's state for this transformed time
            var a = profile.calculate(K*t);
            double Pa = a.position, Va = a.velocity;
            
            // Adjust the original position and velocity according to the time factor between the two states
            return new TrapezoidProfile.State(
                Pa,     // Pb = Pa(K*t)
                K * Va  // Vb = K * Va(K*t)
            );
            
        };
        
    }
    
    @Override
    public double getTotalDuration () {
        return totalDuration;
    }
    
    @Override
    public SwerveTrajectoryPoint getPoint (double time) {
        
        // Get the states of the motion profiles
        var driveState = driveProfile.getState(time);
        var rotationState = rotationProfile.getState(time);
        
        // Calculate drive position and velocity from the motion profile and offsets
        Vector<N2> drivePosition = Vector.from(
            driveDirection,
            driveState.position
        ).add(driveInitialOffset);
        
        Vector<N2> driveVelocity = Vector.from(
            driveDirection,
            driveState.velocity
        );
        
        // Calculate angle and angular velocity from the motion profile and offsets
        double angle = rotationState.position + rotationInitialOffset;
        double angularVelocity = rotationState.velocity;
        
        // Return the represented SwerveTrajectoryPoint
        return new SwerveTrajectoryPoint(drivePosition, angle, driveVelocity, angularVelocity);
        
    }
    
    private static interface MotionProfileWrapper {
    
        public TrapezoidProfile.State getState (double time);
        
        public static MotionProfileWrapper fromTrapezoidProfile (TrapezoidProfile profile) {
            return profile::calculate;
        }
        
    }
    
}
