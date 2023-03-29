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
        
        addTests(
            new SwerveDriveHandler(
                new Pose2d(),
                () -> new Rotation2d(),
                new SwerveModule(0, 1),
                new SwerveModule(0, 0)
            ).generateSubsystemTests()
        );
    }
    
    @Override
    public void stop () {
        
    }
    
    private class SwerveModule extends SwerveModuleBase {
        
        public SwerveModule (double x, double y) {
            super(new Translation2d(x, y));
        }
        
        @Override
        public void driveToRawState(SwerveModuleState state) {}

        @Override
        public SwerveModulePosition getPosition() {
            return new SwerveModulePosition();
        }

        @Override
        public SwerveModuleState getState() {
            return new SwerveModuleState();
        }
        
        @Override
        public void setTurnMotorVoltage (double voltage) { }
        
        @Override
        public void setDriveMotorVoltage (double voltage) { }

        @Override
        public double getMaxDriveSpeedMetersPerSec() {
            return 0;
        }

        @Override
        public void stop() {}
        
    }
    
}
