package claw.hardware.swerve;

import claw.hardware.encoders.AbsoluteEncoderBase;
import claw.hardware.encoders.QuadEncoderBase;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;

public class AdvancedSwerveModule extends SwerveModuleBase {
    
    private final MotorController driveMotor, steerMotor;
    private final QuadEncoderBase driveEncoder;
    private final AbsoluteEncoderBase steerEncoder;
    
    private final SimpleMotorFeedforward driveFeedforwardMetersPerSec;
    private final PIDController driveVelocityPIDControllerMetersPerSec;
    private final PIDController steerPositionPIDControllerRadians;
    
    public AdvancedSwerveModule (
        String identifier,
        Translation2d translation,
        MotorController driveMotor,
        MotorController steerMotor,
        QuadEncoderBase driveEncoder,
        AbsoluteEncoderBase steerEncoder,
        SimpleMotorFeedforward driveMotorFeedforwardMetersPerSec,
        PIDController driveVelocityPIDControllerMetersPerSec,
        PIDController steerPositionPIDControllerRadians
    ) {
        super(identifier, translation);
        this.driveMotor = driveMotor;
        this.steerMotor = steerMotor;
        this.driveEncoder = driveEncoder;
        this.steerEncoder = steerEncoder;
        
        this.driveVelocityPIDControllerMetersPerSec = driveVelocityPIDControllerMetersPerSec;
        this.steerPositionPIDControllerRadians = steerPositionPIDControllerRadians;
        
        this.driveFeedforwardMetersPerSec = driveMotorFeedforwardMetersPerSec;
        driveVelocityPIDControllerMetersPerSec.disableContinuousInput();
        steerPositionPIDControllerRadians.disableContinuousInput(); // This is done manually
    }
    
    @Override
    public double getMaxDriveSpeedMetersPerSec () {
        return driveFeedforwardMetersPerSec.maxAchievableVelocity(
            RobotController.getBatteryVoltage(), 0
        );
    }
    
    @Override
    public double getMaxTurnMotorVoltage () {
        return RobotController.getBatteryVoltage() * 0.8;
    }
    
    @Override
    public void driveToRawState (SwerveModuleState state) {
        commandDriveMotorToVelocity(state.speedMetersPerSecond);
        commandTurnMotorToSetpoint(state.angle);
    }
    
    @Override
    public void setDriveMotorVoltage (double voltage) {
        driveVelocityPIDControllerMetersPerSec.reset();
        driveMotor.setVoltage(voltage);
    }
    
    private void commandDriveMotorToVelocity (double desiredVelocityMetersPerSec) {
        double currentDriveSpeed = getState().speedMetersPerSecond;
        
        driveMotor.setVoltage(
            driveFeedforwardMetersPerSec.calculate(desiredVelocityMetersPerSec) +
            driveVelocityPIDControllerMetersPerSec.calculate(currentDriveSpeed, desiredVelocityMetersPerSec)
        );
    }
    
    @Override
    public void setTurnMotorVoltage (double voltage) {
        steerPositionPIDControllerRadians.reset();
        steerMotor.setVoltage(voltage);
    }
    
    private void commandTurnMotorToSetpoint (Rotation2d desiredRotation) {
        double desiredOffsetRadians = MathUtil.interpolate(-180, 180,
            desiredRotation.minus(getRotation()).getRadians()
        );
        
        double steerVoltage = steerPositionPIDControllerRadians.calculate(0, desiredOffsetRadians);
        
        // do not command the motor if the PID indicates we're at the setpoint
        steerMotor.setVoltage(
            steerPositionPIDControllerRadians.atSetpoint() ? 0 : steerVoltage
        );
    }
    
    @Override
    public SwerveModulePosition getPosition () {
        return new SwerveModulePosition(driveEncoder.getDisplacement(), getRotation());
    }
    
    @Override
    public SwerveModuleState getState () {
        return new SwerveModuleState(driveEncoder.getVelocity(), getRotation());
    }
    
    @Override
    public Rotation2d getRotation () {
        return steerEncoder.getRotation();
    }
    
    @Override
    public void stop () {
        driveVelocityPIDControllerMetersPerSec.reset();
        steerPositionPIDControllerRadians.reset();
        driveMotor.stopMotor();
        steerMotor.stopMotor();
    }
    
}
