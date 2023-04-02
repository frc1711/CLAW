package claw.hardware.swerve.tests;

import claw.LiveValues;
import claw.actions.FunctionalCommand;
import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.math.LinearInterpolator;
import claw.math.Transform;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.wpilibj.Timer;
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
                    
                    // Run the module test command
                    double c = ctx.runLiveValuesGet(liveValues -> new MeasureModuleTurningCommand(ctx.subsystem, module, liveValues));
                    
                }
                
                // 1)   Turn modules while measuring their rotations to determine:
                //   a) Whether or not there's any significant change in the modules' readings at all
                //   b) Whether or not it's changing too eradically to be a reasonable estimate of position
                //   c) The direction of positive movement for the module
                
            })
        );
    }
    
    private static class MeasureModuleTurningCommand extends CommandBase implements FunctionalCommand<Double> {
        
        private static final double TOTAL_DURATION = 15, RAMP_TIME = 3;
        
        private final LiveValues debug;
        private final SwerveModuleBase module;
        private final Transform timeToVoltage;
        private final Timer timer = new Timer();
        
        public MeasureModuleTurningCommand (CLAWSubsystem subsystem, SwerveModuleBase module, LiveValues debug) {
            this.debug = debug;
            this.module = module;
            addRequirements(subsystem);
            
            // TODO: Some more test stuff on the modules
            double maxModuleVoltage = 5; // module.getMaxTurnVoltage();
            
            timeToVoltage = new LinearInterpolator(
                0,                              0,
                RAMP_TIME,                      maxModuleVoltage,
                TOTAL_DURATION - RAMP_TIME,     maxModuleVoltage,
                TOTAL_DURATION,                 0
            ).then(Transform.clamp(0, maxModuleVoltage));
        }
        
        @Override
        public void initialize () {
            timer.reset();
            updateVoltage(0);
        }
        
        @Override
        public void execute () {
            double voltage = timeToVoltage.apply(timer.get());
            updateVoltage(voltage);
        }
        
        @Override
        public void end (boolean interrupted) {
            updateVoltage(0);
        }
        
        private void updateVoltage (double voltage) {
            module.setDriveMotorVoltage(0);
            module.setTurnMotorVoltage(voltage);
            debug.setField("Turn Voltage", voltage);
            debug.setField("Module Rotation (degrees)", module.getRotation().getDegrees());
        }
        
        @Override
        public boolean isFinished () {
            return timer.hasElapsed(TOTAL_DURATION);
        }
        
        @Override
        public Double getValue () {
            return 0.;
        }
        
    }
    
}
