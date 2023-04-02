package claw.hardware.swerve.tests;

import claw.LiveValues;
import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ModuleTurnTest extends SubsystemTest {
    
    public ModuleTurnTest (SwerveDriveHandler swerveDrive) {
        super(
            "moduleTurnTest",
            "A simple module turn test. The robot should be on its side for the duration of this test.",
            TestCommandSupplier.fromComposition(ctx -> {
                
                // Iterate through each swerve module
                for (SwerveModuleBase module : swerveDrive.getModules()) {
                    
                    // Print a message signaling the module we're running the test on
                    ctx.console.println("Running module turning test for module: " + module.getIdentifier());
                    
                    // Wait for just 1.5 seconds before continuing
                    ctx.delay(1.5);
                    
                    
                    
                    // TODO: LiveValuesCommandBase?
                    
                    
                    
                    
                    
                    // Run the module test command
                    ctx.runLiveValues(liveValues -> new MeasureModuleTurningCommand(ctx.subsystem, module, liveValues));
                    
                }
                
                // 1)   Turn modules while measuring their rotations to determine:
                //   a) Whether or not there's any significant change in the modules' readings at all
                //   b) Whether or not it's changing too eradically to be a reasonable estimate of position
                //   c) The direction of positive movement for the module
                
            })
        );
    }
    
    private static class MeasureModuleTurningCommand extends CommandBase {
        
        private final LiveValues debug;
        private final SwerveModuleBase module;
        
        public MeasureModuleTurningCommand (CLAWSubsystem subsystem, SwerveModuleBase module, LiveValues debug) {
            this.debug = debug;
            this.module = module;
            addRequirements(subsystem);
        }
        
        @Override
        public void initialize () {
            module.stop();
        }
        
        @Override
        public void execute () {
            
        }
        
        @Override
        public void end (boolean interrupted) {
            module.stop();
        }
        
    }
    
}
