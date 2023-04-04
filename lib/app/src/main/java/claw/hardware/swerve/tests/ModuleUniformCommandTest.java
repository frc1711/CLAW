package claw.hardware.swerve.tests;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.InstantCommand;

public class ModuleUniformCommandTest extends SubsystemTest {
    
    private static final TestRotation[] testRotations = new TestRotation[]{
        rotation(0),
        rotation(45),
        rotation(90),
        rotation(135),
        rotation(180),
        rotation(225),
        rotation(270),
        rotation(315),
        rotation(360),
        
        rotation(0, 180),
        rotation(180, 0),
        rotation(90, 180),
        rotation(270, 180),
    };
    
    public ModuleUniformCommandTest (SwerveDriveHandler swerveDrive) {
        super(
            "uniformModuleCommand",
            "Drives all swerve modules to different states to confirm they are working properly. " +
            "Prop the robot on its side before running this test.",
            TestCommandSupplier.fromComposition(ctx -> {
                
                for (int i = 0; i < testRotations.length; i ++) {
                    ctx.console.printlnSys("\nTest "+(i+1)+" of "+testRotations.length);
                    ctx.console.println(testRotations[i].getDescription());
                    
                    TestRotation test = testRotations[i];
                    ctx.run(new RunTestRotationCommand(swerveDrive, ctx.subsystem, test));
                }
                
                ctx.run(new StopSwerveDriveCommand(swerveDrive, ctx.subsystem));
                
            })
        );
    }
    
    private static class StopSwerveDriveCommand extends InstantCommand {
        public StopSwerveDriveCommand (SwerveDriveHandler swerveDrive, CLAWSubsystem subsystem) {
            super(swerveDrive::stop, subsystem);
        }
    }
    private static class RunTestRotationCommand extends CommandBase {
        
        private static final double DURATION = 5;
        
        private final SwerveDriveHandler swerveDrive;
        private final Timer timer = new Timer();
        private final TestRotation test;
        
        public RunTestRotationCommand (SwerveDriveHandler swerveDrive, CLAWSubsystem subsystem, TestRotation test) {
            addRequirements(subsystem);
            this.swerveDrive = swerveDrive;
            this.test = test;
        }
        
        @Override
        public void initialize () {
            timer.reset();
            timer.start();
        }
        
        @Override
        public void execute () {
            SwerveModuleState desiredState = test.getModuleState(
                MathUtil.clamp(timer.get() / DURATION, 0, 1)
            );
            
            for (SwerveModuleBase module : swerveDrive.getModules()) {
                module.driveToStateOptimize(desiredState, true);
            }
        }
        
        @Override
        public boolean isFinished () {
            return timer.hasElapsed(DURATION);
        }
        
    }
    
    private static TestRotation rotation (double degrees) {
        return new TestRotation(){
            @Override
            public String getDescription () {
                return "Rotate modules to " + degrees + " degrees.";
            }
            
            @Override
            public SwerveModuleState getModuleState (double p) {
                return new SwerveModuleState(1, Rotation2d.fromDegrees(degrees));
            }
        };
    }
    
    private static TestRotation rotation (double startDegrees, double endDegrees) {
        return new TestRotation(){
            @Override
            public String getDescription () {
                return "Rotate modules continuously from " + startDegrees + " degrees to " + endDegrees + " degrees.";
            }
            
            @Override
            public SwerveModuleState getModuleState (double p) {
                return new SwerveModuleState(1,
                    Rotation2d.fromDegrees(startDegrees).interpolate(Rotation2d.fromDegrees(endDegrees), p)
                );
            }
        };
    }
    
    private static interface TestRotation {
        public String getDescription ();
        public SwerveModuleState getModuleState (double p);
    }
    
}
