package claw.hardware.swerve.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import claw.LiveValues;
import claw.actions.FunctionalCommand;
import claw.actions.compositions.Context.TerminatedContextException;
import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.math.LinearInterpolator;
import claw.math.Transform;
import claw.rct.network.low.ConsoleManager;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Rotation2d;
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
                    ModuleAnalysis analysis = ctx.runLiveValuesGet(
                        liveValues -> new MeasureModuleTurningCommand(ctx.subsystem, module, liveValues)
                    );
                    
                    // Read the analysis to the console
                    analysis.printToConsole(ctx.console);
                    
                }
                
            })
        );
    }
    
    private static class MeasureModuleTurningCommand extends CommandBase implements FunctionalCommand<ModuleAnalysis> {
        
        private static final double
            // The amount of time allowed for the modules to ramp up to max voltage
            RAMP_TIME = 4,
            
            // Analysis only happens when the modules are at max voltage, padding is the amount of time
            // surrounding max voltage where no analysis is happening (modules stabilizing at the max voltage)
            ANALYSIS_PADDING = 0.2,
            
            // Amount of time allowed to analyze the module's rotation
            ANALYSIS_DURATION = 10,
            
            MAX_SPEED_START = RAMP_TIME,
            ANALYSIS_START = MAX_SPEED_START + ANALYSIS_PADDING,
            ANALYSIS_END = ANALYSIS_START + ANALYSIS_DURATION,
            MAX_SPEED_END = ANALYSIS_END + ANALYSIS_PADDING,
            TOTAL_DURATION = MAX_SPEED_END + RAMP_TIME;
        
        private final LiveValues debug;
        private final SwerveModuleBase module;
        private final Transform timeToVoltage;
        private final Timer timer = new Timer();
        
        private final ModuleAnalyzer analyzer;
        
        public MeasureModuleTurningCommand (CLAWSubsystem subsystem, SwerveModuleBase module, LiveValues debug) {
            this.debug = debug;
            this.module = module;
            addRequirements(subsystem);
            
            double maxModuleVoltage = Math.abs(module.getMaxTurnMotorVoltage());
            
            timeToVoltage = new LinearInterpolator(
                0,                  0,
                MAX_SPEED_START,    maxModuleVoltage,
                MAX_SPEED_END,      maxModuleVoltage,
                TOTAL_DURATION,     0
            ).then(Transform.clamp(0, maxModuleVoltage));
            
            analyzer = new ModuleAnalyzer(module, maxModuleVoltage);
        }
        
        @Override
        public void initialize () {
            System.out.println("\n\nINITIALIZING COMMAND\n\n");
            timer.reset();
            timer.start();
            analyzer.reset();
            updateVoltage(0);
        }
        
        @Override
        public void execute () {
            double voltage = timeToVoltage.apply(timer.get());
            
            if (timer.get() >= ANALYSIS_START && timer.get() <= ANALYSIS_END) {
                analyzer.update();
            }
            
            updateVoltage(voltage);
            System.out.println("\n\nEXECUTING COMMAND\n\n");
        }
        
        @Override
        public void end (boolean interrupted) {
            updateVoltage(0);
            System.out.println("\n\nENDING COMMAND\n\n");
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
        public ModuleAnalysis getValue () {
            return analyzer.getAnalysis();
        }
        
    }
    
    private static class ModuleAnalyzer {
        
        private final Timer timer = new Timer();
        private final SwerveModuleBase module;
        private final double moduleDriveVoltage;
        
        private Optional<Rotation2d> lastRotation = Optional.empty();
        private double lastRotationTime = 0;
        private final ArrayList<Double> rotationSpeeds = new ArrayList<>();
        
        public ModuleAnalyzer (SwerveModuleBase module, double moduleDriveVoltage) {
            this.module = module;
            this.moduleDriveVoltage = moduleDriveVoltage;
            reset();
        }
        
        public void update () {
            
            Rotation2d currentRotation = module.getRotation();
            double currentTime = timer.get();
            
            if (lastRotation.isEmpty()) {
                
                lastRotation = Optional.of(currentRotation);
                lastRotationTime = currentTime;
                
            } else {
                
                Rotation2d prevRotation = lastRotation.get();
                double deltaTime = currentTime - lastRotationTime;
                
                lastRotation = Optional.of(currentRotation);
                lastRotationTime = currentTime;
                
                double degreesOffset = MathUtil.inputModulus(
                    currentRotation.minus(prevRotation).getDegrees(),
                    -180, 180
                );
                
                double turnSpeed = degreesOffset / deltaTime;
                rotationSpeeds.add(turnSpeed);
                
            }
            
            module.getRotation().getDegrees();
            
        }
        
        public ModuleAnalysis getAnalysis () {
            
            double mean = getMean(rotationSpeeds);
            double stddev = getStandardDeviation(rotationSpeeds, mean);
            double ubound = mean + 2 * stddev;
            double lbound = mean - 2 * stddev;
            
            boolean readCCWRotation = mean > 0;
            
            TurnVoltageCorrelation correlation;
            if (Math.abs(mean) < 2) {
                
                if (stddev < 10) {
                    // Less than an average of 2 degrees/sec speed, tight center around zero
                    correlation = TurnVoltageCorrelation.NO_SIGNIFICANT_MEASUREMENT;
                } else {
                    // Average speed is very low, but stddev indicates the measurement is erratic
                    correlation = TurnVoltageCorrelation.ERRATIC_MEASUREMENT;
                }
                
            } else if ((lbound > 0) != (ubound > 0)) {
                
                // Upper and lower bounds are not on the same side of zero,
                // one is negative and one is positive
                correlation = TurnVoltageCorrelation.ERRATIC_MEASUREMENT;
                
            } else {
                
                // Stable measurement, positive or negative
                boolean isPositiveCCW = readCCWRotation == (moduleDriveVoltage > 0);
                correlation = isPositiveCCW
                    ? TurnVoltageCorrelation.POSITIVE_COUNTERCLOCKWISE
                    : TurnVoltageCorrelation.NEGATIVE_COUNTERCLOCKWISE;
                
            }
            
            return new ModuleAnalysis(readCCWRotation, mean, stddev, correlation);
            
        }
        
        public void reset () {
            timer.reset();
            timer.start();
            lastRotation = Optional.empty();
            rotationSpeeds.clear();
        }
        
    }
    
    private static enum TurnVoltageCorrelation {
        
        POSITIVE_COUNTERCLOCKWISE,
        
        NEGATIVE_COUNTERCLOCKWISE,
        
        NO_SIGNIFICANT_MEASUREMENT,
        
        ERRATIC_MEASUREMENT
        
    }
    
    private static double getMean (List<Double> values) {
        double sum = 0;
        for (Double value : values) {
            sum += value;
        }
        
        return sum / values.size();
    }
    
    private static double getStandardDeviation (List<Double> values, double mean) {
        
        double sum = 0;
        for (Double value : values) {
            sum += Math.pow(value - mean, 2);
        }
        
        return Math.sqrt(sum / values.size());
        
    }
    
    private static record ModuleAnalysis (
        boolean readCCWRotation,
        double readMeanDegreesPerSec,
        double readStddevDegreesPerSec,
        TurnVoltageCorrelation voltageCorrelation
    ) {
        
        public void printToConsole (ConsoleManager console) throws TerminatedContextException {
            
            if (voltageCorrelation == TurnVoltageCorrelation.ERRATIC_MEASUREMENT) {
                
                console.printlnErr("Measurement from the swerve module's encoder was erratic.");
                
            } else if (voltageCorrelation == TurnVoltageCorrelation.NO_SIGNIFICANT_MEASUREMENT) {
                
                console.printlnErr("The moduler's encoder measured no significant speed.");
                
            } else {
                
                String dirDescTop = readCCWRotation ? "counterclockwise" : "clockwise";
                String dirDescBot = !readCCWRotation ? "counterclockwise" : "clockwise";
                
                // Print the message describing the read direction of rotation
                String msg = ConsoleManager.formatMessage(
                    "The encoder measured a "+dirDescTop+" rotation from the top-down point of view (or "+dirDescBot+" " +
                    "rotation from the bottom-up point of view, as you probably are seeing it). Ensure this accurately describes " +
                    "the direction the module rotated in before moving on. " +
                    "If it does not, you'll have to negate this module's encoder reading before moving " + 
                    "on to the rest of the test.\n" + 
                    "(Press any key to continue)"
                );
                console.print(msg);
                
                // Wait for the user to press a key
                while (!console.hasInputReady()) { }
                console.println("\n");
                
                // Print measurements
                String voltageCorrelationDesc = voltageCorrelation == TurnVoltageCorrelation.POSITIVE_COUNTERCLOCKWISE
                    ? "A positive voltage applied to the motor leads to a counterclockwise turn, positive increase in module.getRotation()"
                    : "A negative voltage applied to the motor leads to a counterclockwise turn, positive increase in module.getRotation()";
                console.println("Voltage to turn correlation: " + voltageCorrelationDesc);
                
            }
            
            console.println("Mean measured turn speed: " + readMeanDegreesPerSec + " deg/sec");
            console.println("Standard deviation of measured turn speed: " + readStddevDegreesPerSec + " deg/sec");
            
        }
        
    }
    
}
