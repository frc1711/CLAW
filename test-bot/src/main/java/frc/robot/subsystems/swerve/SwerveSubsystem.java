package frc.robot.subsystems.swerve;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class SwerveSubsystem extends CLAWSubsystem {
    
    private final TestSwerveModule[] swerveModules = new TestSwerveModule[]{
        new TestSwerveModule("FRONT LEFT", new Translation2d(1, 1)),
        new TestSwerveModule("REAR RIGHT", new Translation2d(-1, -1))
    };
    
    private final SwerveDriveHandler swerveDrive = new SwerveDriveHandler(new Pose2d(), () -> new Rotation2d(), swerveModules);
    
    public SwerveSubsystem () {
        addTests(swerveDrive.generateSubsystemTests());
    }
    
    public void stop () {
        swerveDrive.stop();
    }
    
    @Override
    public void periodic () {
        for (TestSwerveModule module : swerveModules) {
            module.periodicUpdate();
        }
    }
    
}
