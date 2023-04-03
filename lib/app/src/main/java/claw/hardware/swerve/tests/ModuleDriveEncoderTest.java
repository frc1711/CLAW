package claw.hardware.swerve.tests;

import java.io.Console;

import claw.LiveValues;
import claw.actions.FunctionalCommand;
import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.math.LinearInterpolator;
import claw.math.Transform;
import claw.rct.console.ConsoleManager;
import claw.rct.console.ConsoleUtils;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ModuleDriveEncoderTest extends SubsystemTest {
    
    public ModuleDriveEncoderTest (SwerveDriveHandler swerveDrive) {
        super(
            "moduleDriveEncoder",
            "A test which can estimate drive encoder factors to get better distance measurements for swerve drive. This " +
            "is important for swerve drive odometry and, depending on the implementation details of the swerve modules " +
            "(whether they use a drive motor feedforward and PID loop) basic module functionality.",
            TestCommandSupplier.fromComposition(ctx -> {
                
                for (SwerveModuleBase module : swerveDrive.getModules()) {
                    
                    double speedProp = ConsoleUtils.getDoubleValue(
                        ctx.console,
                        "What proportion of max speed should the module drive at? [0.01, 1] ",
                        0.01,
                        1
                    );
                    
                    double duration = ConsoleUtils.getDoubleValue(
                        ctx.console,
                        "How long should the test run for, in seconds? [5, 120] ",
                        5,
                        120
                    );
                    
                    double diameterInches = ConsoleUtils.getDoubleValue(
                        ctx.console,
                        "What is the diameter of the module wheel, in inches? ",
                        1, 40
                    );
                    
                    double circumferenceMeters = Units.inchesToMeters(diameterInches) * Math.PI;
                    ctx.console.println("The wheel has a circumference of " + circumferenceMeters + " meters.");
                    
                    ctx.console.println("Prepare to count rotations on module: " + module.getIdentifier());
                    ConsoleUtils.pressKeyToContinue(ctx.console);
                    
                    double measuredMetersTraveled = ctx.runLiveValuesGet(
                        liveValues -> new CountModuleRotations(module, ctx.subsystem, liveValues, duration, speedProp)
                    );
                    
                    double rotationsCounter = ConsoleUtils.getDoubleValue(
                        ctx.console,
                        "How many rotations of the module did you count? ",
                        0.1,
                        10000
                    );
                    
                    double actualMetersTraveled = rotationsCounter * circumferenceMeters;
                    ctx.console.println("Actual travel distance (meters):   " + actualMetersTraveled);
                    ctx.console.println("Measured travel distance (meters): " + measuredMetersTraveled);
                    
                    if (measuredMetersTraveled != 0) {
                        
                        // actual = C * measured
                        // C = actual / measured
                        double proportionScale = actualMetersTraveled / measuredMetersTraveled;
                        ctx.console.println(ConsoleManager.formatMessage(
                            "By this estimate, to improve the accuracy of the measured distance, you must multiply " +
                            "the measurement by: "
                        ));
                        
                        ctx.console.println(""+proportionScale);
                        ctx.console.println(ConsoleManager.formatMessage(
                            "Note that this measurement is an approximation, and to get a better measurement, you will have " +
                            "to test swerve drive on the ground."
                        ));
                        
                    } else {
                        ctx.console.printlnErr("There must be something wrong, because getPosition() didn't measure any travel distance.");
                    }
                    
                }
                
            })
        );
    }
    
    private static class CountModuleRotations extends CommandBase implements FunctionalCommand<Double> {
        
        private static final double RAMP_TIME = 1;
        
        private final Timer timer = new Timer();
        private final SwerveModuleBase module;
        private final LiveValues liveValues;
        
        private final double durationSecs;
        private final double maxRunSpeed;
        private final Transform speedFromTime;
        
        private double initialDistanceMeters = 0;
        
        public CountModuleRotations (SwerveModuleBase module, CLAWSubsystem subsystem, LiveValues liveValues, double maxSpeedDurationSecs, double driveSpeedProportion) {
            addRequirements(subsystem);
            this.module = module;
            this.liveValues = liveValues;
            
            durationSecs = maxSpeedDurationSecs + RAMP_TIME*2;
            maxRunSpeed = Math.abs(module.getMaxDriveMotorVoltage() * driveSpeedProportion);
            speedFromTime = new LinearInterpolator(
                0,                              0,
                RAMP_TIME,                      maxRunSpeed,
                RAMP_TIME+maxSpeedDurationSecs, maxRunSpeed,
                durationSecs,                   0
            ).then(Transform.clamp(0, driveSpeedProportion));
        }
        
        @Override
        public void initialize () {
            module.stop();
            timer.reset();
            timer.start();
            updateLiveValues();
        }
        
        @Override
        public void execute () {
            module.setTurnMotorVoltage(0);
            module.setDriveMotorVoltage(speedFromTime.apply(timer.get()));
            updateLiveValues();
        }
        
        @Override
        public void end (boolean interrupted) {
            module.stop();
            updateLiveValues();
        }
        
        private double getDistanceTraveled () {
            return module.getPosition().distanceMeters - initialDistanceMeters;
        }
        
        private void updateLiveValues () {
            liveValues.setField("Measured Distance Traveled (meters)", getDistanceTraveled());
        }
        
        @Override
        public boolean isFinished () {
            return timer.hasElapsed(durationSecs);
        }
        
        @Override
        public Double getValue () {
            return getDistanceTraveled();
        }
        
    }
    
}
