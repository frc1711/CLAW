package claw.hardware.swerve.tests;

import claw.LiveValues;
import claw.actions.FunctionalCommand;
import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.rct.console.ConsoleManager;
import claw.rct.console.ConsoleUtils;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class ModuleFeedforwardTest extends SubsystemTest {
    
    public ModuleFeedforwardTest (SwerveDriveHandler swerveDrive) {
        super(
            "tuneModuleFeedforward",
            "A test which (poorly) emulates WPILib's sysid tool for swerve drive, estimating kS, kV and kA terms for " +
            "swerve module feedforwards.",
            TestCommandSupplier.fromComposition(ctx -> {
                
                ctx.console.println(ConsoleManager.formatMessage(
                    "Set the robot on the ground, with plenty of space in front of it. " +
                    "Ensure you have tuned the drive encoders before running this test. Otherwise, " +
                    "the kS, kV and kA measurements will be useless."
                ));
                
                // Dynamic test
                double freeDistFeet = ConsoleUtils.getDoubleValue(
                    ctx.console,
                    "How much free distance is there in front of the robot, in feet? ",
                    0.1, 50
                );
                
                double driveVoltage = ConsoleUtils.getDoubleValue(
                    ctx.console,
                    "What voltage should be applied to the drive motors on the swerve modules? (Start really low) ",
                    1, 10
                );
                
                if (!ConsoleUtils.getYesNo(ctx.console, "Continue to run test? ")) return;
                ctx.run(new TurnModulesForward(swerveDrive, ctx.subsystem));
                double[] accelerations = ctx.runLiveValuesGet(
                    liveValues -> new DynamicTest(swerveDrive, ctx.subsystem, liveValues, Units.feetToMeters(freeDistFeet), driveVoltage)
                );
                
                SwerveModuleBase[] modules = swerveDrive.getModules();
                for (int i = 0; i < modules.length; i ++) {
                    ctx.console.printlnSys("Average acceleration for module: " + modules[i].getIdentifier());
                    ctx.console.println("Average = "+accelerations[i]+" m/s^2");
                }
                
            })
        );
    }
    
    private static class DynamicTest extends CommandBase implements FunctionalCommand<double[]> {
        
        private final SwerveDriveHandler swerveDrive;
        
        private final double stopAnalysisDist;
        private final SlewRateLimiter voltageRateLimiter;
        private final double driveVoltage;
        private final LiveValues liveValues;
        
        private ArrayAverager accelerationAverager;
        private TestAnalyzer analyzer;
        private double[] initialModulePositions;
        private boolean hasFinished = false;
        
        public DynamicTest (SwerveDriveHandler swerveDrive, CLAWSubsystem subsystem, LiveValues liveValues, double freeDistMeters, double driveVoltage) {
            addRequirements(subsystem);
            this.swerveDrive = swerveDrive;
            this.driveVoltage = driveVoltage;
            this.liveValues = liveValues;
            
            stopAnalysisDist = freeDistMeters * 0.6;
            voltageRateLimiter = new SlewRateLimiter(driveVoltage*2, -driveVoltage*2, driveVoltage);
        }
        
        @Override
        public void initialize () {
            swerveDrive.stop();
            hasFinished = false;
            analyzer = new TestAnalyzer(swerveDrive);
            accelerationAverager = new ArrayAverager(swerveDrive.getModules().length);
            initialModulePositions = analyzer.getModulePositions();
            updateLiveValues();
        }
        
        @Override
        public void execute () {
            analyzer.update();
            
            double distMeters = getMeasuredDistMeters();
            double targetVoltage;
            
            if (distMeters < stopAnalysisDist) {
                accelerationAverager.accumulate(analyzer.accelerations);
                
                targetVoltage = voltageRateLimiter.calculate(driveVoltage);
            } else {
                targetVoltage = voltageRateLimiter.calculate(0);
            }
            
            if (Math.abs(targetVoltage) < 0.1) hasFinished = true;
            
            for (SwerveModuleBase module : swerveDrive.getModules()) {
                module.setDriveMotorVoltage(targetVoltage);
            }
            
            updateLiveValues();
        }
        
        private double getMeasuredDistMeters () {
            SwerveModuleBase[] modules = swerveDrive.getModules();
            double distSum = 0;
            
            for (int i = 0; i < modules.length; i ++) {
                distSum += Math.abs(modules[i].getPosition().distanceMeters - initialModulePositions[i]);
            }
            
            return distSum / modules.length;
            
        }
        
        @Override
        public void end (boolean interrupted) {
            swerveDrive.stop();
            updateLiveValues();
        }
        
        private void updateLiveValues () {
            SwerveModuleBase[] modules = swerveDrive.getModules();
            double[] accelerations = accelerationAverager.getAverage();
            for (int i = 0; i < modules.length; i ++) {
                liveValues.setField(modules[i].getIdentifier() + " accel", accelerations[i]);
            }
        }
        
        @Override
        public boolean isFinished () {
            return hasFinished;
        }
        
        @Override
        public double[] getValue () {
            return accelerationAverager.getAverage();
        }
        
    }
    
    private static class TestAnalyzer {
        
        private final SwerveModuleBase[] modules;
        
        private double[] positions;
        private double[] velocities;
        private double[] accelerations;
        
        private double[] lastVelocitiesForAccel;
        private double lastAccelMeasureTime = 0;
        
        public TestAnalyzer (SwerveDriveHandler swerveDrive) {
            this.modules = swerveDrive.getModules();
            getModuleVelocities();
            update();
        }
        
        public void update () {
            positions = getModulePositions();
            velocities = getModuleVelocities();
            accelerations = getModuleAccelerations();
        }
        
        private double[] getModuleAccelerations () {
            if (lastAccelMeasureTime == 0) {
                lastVelocitiesForAccel = getModuleVelocities();
                lastAccelMeasureTime = Timer.getFPGATimestamp();
                return new double[modules.length];
            }
            
            double deltaTime = Timer.getFPGATimestamp() - lastAccelMeasureTime;
            double[] mVelocities = getModuleVelocities();
            double[] mAccel = new double[modules.length];
            
            for (int i = 0; i < mAccel.length; i ++) {
                mAccel[i] = (mVelocities[i] - lastVelocitiesForAccel[i]) / deltaTime;
            }
            
            lastVelocitiesForAccel = mVelocities;
            return mAccel;
        }
        
        private double[] getModuleVelocities () {
            double[] velocities = new double[modules.length];
            for (int i = 0; i < modules.length; i ++) {
                velocities[i] = modules[i].getState().speedMetersPerSecond;
            }
            
            return velocities;
        }
        
        private double[] getModulePositions () {
            double[] velocities = new double[modules.length];
            for (int i = 0; i < modules.length; i ++) {
                velocities[i] = modules[i].getPosition().distanceMeters;
            }
            return velocities;
        }
        
    }
    
    private static class ArrayAverager {
        
        private int count = 0;
        private double[] sumValues;
        
        public ArrayAverager (int size) {
            sumValues = new double[size];
        }
        
        public void accumulate (double[] values) {
            if (count == 0) {
                count ++;
                sumValues = values;
            } else {
                for (int i = 0; i < sumValues.length; i ++) {
                    sumValues[i] += values[i];
                }
                count ++;
            }
        }
        
        public double[] getAverage () {
            if (count == 0) return sumValues;
            
            double[] avg = new double[sumValues.length];
            for (int i = 0; i < avg.length; i ++) {
                avg[i] = sumValues[i] / count;
            }
            
            return avg;
        }
        
    }
    
}
