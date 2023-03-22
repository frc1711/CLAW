package claw.hardware.swerve;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;

/**
 * A swerve module which can drive and turn to a given angle.
 */
public abstract class SwerveModuleBase {
    
    private final Translation2d translation;
    
    /**
     * Create a new {@link SwerveModuleBase} with a given translation from the center of the robot.
     * @param translation The translation of the swerve module from the center of the robot, in meters.
     */
    public SwerveModuleBase (Translation2d translation) {
        this.translation = translation;
    }
    
    /**
     * Get the translation of the swerve module from the center of the robot.
     * @return  The tranlation from the center of the robot, in meters.
     */
    public Translation2d getTranslation () {
        return translation;
    }
    
    /**
     * Drive to a given {@link SwerveModuleState}, optimizing the state for the current module position so that
     * it drives in an equivalent direction.
     * @param desiredState  The desired swerve module state.
     * @param alwaysTurn    Whether or not to turn the swerve module even if the drive speed is zero.
     */
    public void driveToStateOptimize (SwerveModuleState desiredState, boolean alwaysTurn) {
        Rotation2d currentAngle = getPosition().angle;
        
        // Only drive if the speed is nonzero (or drive if alwaysTurn is true)
        if (alwaysTurn || desiredState.speedMetersPerSecond > 0) {
            // Optimize the swerve module state for the angle
            SwerveModuleState optimizedState = SwerveModuleState.optimize(desiredState, currentAngle);
            
            // Drive to the optimized state
            driveToRawState(optimizedState);
        } else {
            // Drive to the current module's angle so that we don't continue moving
            driveToRawState(new SwerveModuleState(0, currentAngle));
        }
    }
    
    /**
     * Drive to the given {@link SwerveModuleState} without any optimization of the state. Do not
     * fail to turn the module if the state's drive speed is zero, do not optimize the module's angle.
     * Note that the rotation of the module's state increases counter-clockwise, with zero being directly
     * forward.
     * @param state The target state of the swerve module.
     */
    public abstract void driveToRawState (SwerveModuleState state);
    
    /**
     * Get a {@link SwerveModulePosition} describing the position of the swerve module. Note that
     * the rotation of the module's position increases counter-clockwise, with zero being directly
     * forward.
     * @return  The swerve module's current position.
     */
    public abstract SwerveModulePosition getPosition ();
    
    /**
     * Stop all movement of the swerve module immediately. Make sure to reset
     * any relevant PID loops or filters.
     */
    public abstract void stop ();
    
}
