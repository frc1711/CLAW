package frc.robot.subsystems.swerve;

import claw.hardware.swerve.SwerveModuleBase;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;

public class TestSwerveModule extends SwerveModuleBase {

    private final SimpleMotorFeedforward driveSpeedFeedforward = new SimpleMotorFeedforward(0.1, 2);
    private final PIDController turnPID = new PIDController(0.1, 0, 0);
    
    private double lastDriveTime = 0;
    
    private double currentRotationDeg = Math.random() * 360;
    private double currentDriveDist = Math.random() * 100;
    
    private double driveMotorVoltage = 0, turnMotorVoltage = 0;
    private double driveVelocity = 0, turnVelocity = 0;
    
    public TestSwerveModule (String identifier, Translation2d translation) {
        super(identifier, translation);
        turnPID.setTolerance(1);
    }
    
    @Override
    public void driveToRawState(SwerveModuleState state) {
        
        double turnVoltage = turnPID.calculate(currentRotationDeg, state.angle.getDegrees());
        double driveVoltage = driveSpeedFeedforward.calculate(state.speedMetersPerSecond);
        
        setTurnMotorVoltage(turnPID.atSetpoint() ? 0 : turnVoltage);
        setDriveMotorVoltage(driveVoltage);
        
    }

    @Override
    public void setDriveMotorVoltage(double voltage) {
        driveMotorVoltage = voltage;
    }

    @Override
    public double getMaxTurnMotorVoltage() {
        return 8;
    }

    @Override
    public void setTurnMotorVoltage(double voltage) {
        turnMotorVoltage = voltage;
    }
    
    @Override
    public double getMaxDriveMotorVoltage () {
        return 8;
    }

    @Override
    public SwerveModulePosition getPosition() {
        return new SwerveModulePosition(currentDriveDist, getRotation());
    }

    @Override
    public SwerveModuleState getState() {
        // TODO: Fix speed here
        return new SwerveModuleState(currentDriveDist, getRotation());
    }

    @Override
    public Rotation2d getRotation() {
        // // Erratically broken
        // return Rotation2d.fromDegrees(Math.random() > 0.05 ? currentRotationDeg : 0);
        
        // // Broken (no changing measurement)
        // return new Rotation2d(0.32);
        
        // Working properly
        return Rotation2d.fromDegrees(currentRotationDeg);
    }

    @Override
    public double getMaxDriveSpeedMetersPerSec() {
        return 3;
    }

    @Override
    public void stop() {
        setDriveMotorVoltage(0);
        setTurnMotorVoltage(0);
    }
    
    public void periodicUpdate () {
        
        driveVelocity = MathUtil.applyDeadband(driveMotorVoltage*5, 0.05) + Math.random()*0.04;
        turnVelocity = MathUtil.applyDeadband(turnMotorVoltage*50, 2) + Math.random()*0.3;
        driveMotorVoltage = 0;
        turnMotorVoltage = 0;
        
        double deltaTime = (lastDriveTime == 0) ? 0 : Timer.getFPGATimestamp() - lastDriveTime;
        lastDriveTime = Timer.getFPGATimestamp();
        
        currentDriveDist += driveVelocity * deltaTime;
        currentRotationDeg += turnVelocity * deltaTime;
        
    }
    
}
