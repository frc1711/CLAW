package frc.robot.subsystems;

import java.nio.channels.AsynchronousServerSocketChannel;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import claw.subsystems.SubsystemTest.TestCommandSupplier;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.PrintCommand;

public class TestSubsystem extends CLAWSubsystem {
    
    public TestSubsystem () {
        addTests(new SubsystemTest(
            "exampleTest",
            "Example description.",
            TestCommandSupplier.fromComposition(ctx -> {
                for (int i = 0; i < 5; i ++) {
                    ctx.run(new PrintCommand("This is a test print"));
                    ctx.delay(i * 0.2);
                }
            })
        ));
    }
    
    @Override
    public void stop () {
        
    }
    
}
