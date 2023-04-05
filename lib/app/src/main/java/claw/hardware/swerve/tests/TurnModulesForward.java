package claw.hardware.swerve.tests;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TurnModulesForward extends CommandBase {
        
    private final SwerveDriveHandler swerveDrive;
    private final Timer timer = new Timer();
    
    public TurnModulesForward (SwerveDriveHandler swerveDrive, CLAWSubsystem subsystem) {
        this.swerveDrive = swerveDrive;
        addRequirements(subsystem);
    }
    
    @Override
    public void initialize () {
        swerveDrive.stop();
        timer.reset();
        timer.start();
    }
    
    @Override
    public void execute () {
        for (SwerveModuleBase module : swerveDrive.getModules()) {
            
            // Find the nearest rotation that is exactly equivalent to 0 degrees
            double degreesOffsetFromZero = MathUtil.inputModulus(module.getRotation().getDegrees(), -180, 180);
            Rotation2d targetRotation = module.getRotation().minus(Rotation2d.fromDegrees(degreesOffsetFromZero));
            
            // Drive the module without optimizing rotation
            module.driveToRawState(new SwerveModuleState(0, targetRotation));
            
        }
    }
    
    @Override
    public void end (boolean interrupted) {
        swerveDrive.stop();
    }
    
    @Override
    public boolean isFinished () {
        return timer.hasElapsed(2);
    }
    
}
