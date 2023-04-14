package claw.hardware.swerve;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;

/**
 * A swerve module which can drive and turn to a given angle.
 */
public abstract class SwerveModuleBase implements Sendable {
    
    private final Translation2d translation;
    private final String identifier;
    
    /**
     * Create a new {@link SwerveModuleBase} with a given translation from the center of the robot.
     * @param identifier    A simple {@code String} identifier which can be used to describe this swerve module
     * to the user.
     * @param translation   The translation of the swerve module from the center of the robot, in meters.
     */
    public SwerveModuleBase (String identifier, Translation2d translation) {
        this.identifier = identifier;
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
     * Get some identifier which can be used to distinguish this module from the others.
     * @return  A name which can be used to identify this module.
     */
    public String getIdentifier () {
        return identifier;
    }
    
    /**
     * Drive to a given {@link SwerveModuleState}, optimizing the state for the current module position so that
     * it drives in an equivalent direction.
     * @param desiredState  The desired swerve module state.
     * @param alwaysTurn    Whether or not to turn the swerve module even if the drive speed is zero.
     */
    public void driveToStateOptimize (SwerveModuleState desiredState, boolean alwaysTurn) {
        Rotation2d currentAngle = getRotation();
        
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
     * Get the maximum allowable voltage (positive) to use with {@link #setDriveMotorVoltage(double)}.
     * @return  The maximum allowable drive motor voltage.
     */
    public abstract double getMaxDriveMotorVoltage ();
    
    /**
     * Set the drive motor's voltage. This is used by automatically generated subsystem tests.
     * @param voltage   The signed voltage to set the drive motor to.
     */
    public abstract void setDriveMotorVoltage (double voltage);
    
    /**
     * Get the maximum allowable voltage (positive) to use with {@link #setTurnMotorVoltage(double)}.
     * @return  The maximum allowable turn motor voltage.
     */
    public abstract double getMaxTurnMotorVoltage ();
    
    /**
     * Set the turn motor's voltage. This is used by automatically generated subsystem tests.
     * @param voltage   The signed voltage to set the turn motor to.
     */
    public abstract void setTurnMotorVoltage (double voltage);
    
    /**
     * Get a {@link SwerveModulePosition} describing the measured position of the swerve module. Note that
     * the rotation of the module's position increases counter-clockwise, with zero being directly
     * forward.
     * @return  The swerve module's current position.
     */
    public abstract SwerveModulePosition getPosition ();
    
    /**
     * Get a {@link SwerveModuleState} describing the measured state of the swerve module. Note
     * that the rotation of the module's state increases counter-clockwise, with zero being directly
     * forward.
     * @return  The swerve module's current state.
     */
    public abstract SwerveModuleState getState ();
    
    /**
     * Get the {@link Rotation2d} of the swerve module, counter-clockwise-positive.
     * @return The current rotation of the module.
     */
    public abstract Rotation2d getRotation ();
    
    /**
     * Get the maximum attainable drive speed of this swerve module at this point in time.
     * @return The max attainable drive speed in meters per second.
     */
    public abstract double getMaxDriveSpeedMetersPerSec ();
    
    /**
     * Stop all movement of the swerve module immediately. Make sure to reset
     * any relevant PID loops or filters.
     */
    public abstract void stop ();
    
    @Override
    public void initSendable (SendableBuilder builder) {
        builder.addStringProperty("Identifier", this::getIdentifier, null);
        builder.addDoubleProperty("Measured Rotation (deg)", () -> getRotation().getDegrees(), null);
        builder.addDoubleProperty("Measured Displacement (m)", () -> getPosition().distanceMeters, null);
        builder.addDoubleProperty("Measured Velocity (m/s)", () -> getState().speedMetersPerSecond, null);
    }
    
}
