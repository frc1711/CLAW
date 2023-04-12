package claw.hardware.swerve.tests;

import claw.LiveValues;
import claw.actions.FunctionalCommand;
import claw.actions.compositions.SubsystemTestCompositionContext;
import claw.actions.compositions.Context.TerminatedContextException;
import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.math.LinearInterpolator;
import claw.math.Transform;
import claw.rct.base.console.ConsoleManager;
import claw.rct.base.console.ConsoleUtils;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModuleState;
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
                
                int operationChoice = ConsoleUtils.getStringAnswer(
                    ctx.console,
                    "Run the drive test (drive) or count test (count)? ",
                    false,
                    "drive",
                    "count"
                );
                
                if (operationChoice == 0) {
                    runDriveTest(ctx, swerveDrive);
                } else {
                    runCountTest(ctx, swerveDrive);
                }
                
                // TODO: Include running a test to ensure the derivative of the drive position roughly matches the drive velocity
                
            })
        );
    }
    
    private static void runDriveTest (
        SubsystemTestCompositionContext<?> ctx,
        SwerveDriveHandler swerveDrive
    ) throws TerminatedContextException {
        
        ctx.console.println("Set the robot on the ground so the swerve drive can drive as it would in a match.");
        ctx.console.println("When you continue, the modules will rotate to all face in one direction.");
        if (!ConsoleUtils.getYesNo(ctx.console, "Continue? ")) return;
        
        // Run the TurnModulesForward command with a deadline (cannot use WPILib .withTimeout() because this leads to)
        ctx.run(new TurnModulesForward(swerveDrive, ctx.subsystem));
        
        double durationSecs = ConsoleUtils.getDoubleValue(
            ctx.console,
            "How long should swerve drive run for? (secs) ",
            1, 10
        );
        
        ctx.console.println(ConsoleManager.formatMessage(
            "Note that the following speed is simply what is passed to the swerve modules, " +
            "by the nature of this test these speeds may be wildly inaccurate (because you " +
            "are testing to determine the actual encoder speeds):"
        ));
        
        double approxForwardSpeed = ConsoleUtils.getDoubleValue(
            ctx.console,
            "At what speed should the robot drive forward? (m/s) ",
            0.01,
            30
        );
        
        ctx.console.println("Preparing to drive forwards.");
        if (!ConsoleUtils.getYesNo(ctx.console, "Continue? ")) return;
        
        double[] distanceDiffs = ctx.runLiveValuesGet(
            liveValues -> new DriveSwerveForward(swerveDrive, ctx.subsystem, liveValues, approxForwardSpeed, durationSecs)
        );
        
        double realDistanceDiffInches = ConsoleUtils.getDoubleValue(
            ctx.console,
            "What was the real distance traveled by the robot, in inches? ",
            1,
            12*50
        );
        
        double realDistanceDiffMeters = Units.inchesToMeters(realDistanceDiffInches);
        
        SwerveModuleBase[] modules = swerveDrive.getModules();
        for (int i = 0; i < modules.length; i ++) {
            ctx.console.printlnSys("Module: " + modules[i].getIdentifier());
            ctx.console.println("  Encoder measured distance traveled (meters): " + distanceDiffs[i]);
            
            if (distanceDiffs[i] != 0) {
                
                double factor = realDistanceDiffMeters / distanceDiffs[i];
                ctx.console.println("  Multiply measurement by " + factor + " to get real distance");
                
            } else {
                
                ctx.console.printlnErr("The encoder measured distance difference was zero. Something is wrong.");
                
            }
            
            ctx.console.println("");
            
        }
        
    }
    
    private static class DriveSwerveForward extends CommandBase implements FunctionalCommand<double[]> {
        
        private static final double RAMP_TIME = 0.5;
        
        private final Timer timer = new Timer();
        private final SwerveDriveHandler swerveDrive;
        private final LiveValues liveValues;
        private final Transform timeToSpeed;
        private final double totalDurationSecs;
        private double[] initialDistances;
        
        public DriveSwerveForward (SwerveDriveHandler swerveDrive, CLAWSubsystem subsystem, LiveValues liveValues, double driveSpeedMetersPerSec, double durationSecs) {
            this.swerveDrive = swerveDrive;
            this.liveValues = liveValues;
            addRequirements(subsystem);
            
            driveSpeedMetersPerSec = Math.abs(driveSpeedMetersPerSec);
            
            totalDurationSecs = RAMP_TIME*2 + durationSecs;
            timeToSpeed = new LinearInterpolator(
                0,                      0,
                RAMP_TIME,              driveSpeedMetersPerSec,
                RAMP_TIME+durationSecs, driveSpeedMetersPerSec,
                totalDurationSecs,      0
            ).then(Transform.clamp(0, driveSpeedMetersPerSec));
        }
        
        @Override
        public void initialize () {
            initialDistances = getModuleDistances();
            swerveDrive.stop();
            timer.reset();
            timer.start();
            updateLiveValues();
        }
        
        @Override
        public void execute () {
            swerveDrive.drive(new ChassisSpeeds(getDriveSpeed(), 0, 0));
            updateLiveValues();
        }
        
        @Override
        public void end (boolean interrupted) {
            swerveDrive.stop();
            updateLiveValues();
        }
        
        private double getDriveSpeed () {
            return timeToSpeed.apply(timer.get());
        }
        
        private void updateLiveValues () {
            liveValues.setField("Target Drive Speed", getDriveSpeed());
            
            SwerveModuleBase[] modules = swerveDrive.getModules();
            double[] distanceDiffs = getModuleDistanceDifferences();
            for (int i = 0; i < modules.length; i ++) {
                liveValues.setField("Module \""+modules[i].getIdentifier()+"\" Distance Traveled (meters)", distanceDiffs[i]);
            }
        }
        
        @Override
        public boolean isFinished () {
            return timer.hasElapsed(totalDurationSecs);
        }
        
        @Override
        public double[] getValue () {
            return getModuleDistanceDifferences();
        }
        
        private double[] getModuleDistanceDifferences () {
            double[] currentDistances = getModuleDistances();
            double[] diffs = new double[currentDistances.length];
            for (int i = 0; i < diffs.length; i ++) {
                // TODO: Use signed displacement
                diffs[i] = Math.abs(currentDistances[i] - initialDistances[i]);
            }
            
            return diffs;
        }
        
        private double[] getModuleDistances () {
            SwerveModuleBase[] modules = swerveDrive.getModules();
            double[] distances = new double[modules.length];
            for (int i = 0; i < distances.length; i ++) {
                distances[i] = modules[i].getPosition().distanceMeters;
            }
            
            return distances;
        }
        
    }
    
    private static void runCountTest (
        SubsystemTestCompositionContext<?> ctx,
        SwerveDriveHandler swerveDrive
    ) throws TerminatedContextException {
        
        ctx.console.println("Prop up the robot so that the modules can drive freely, without resistance.");
        ConsoleUtils.pressKeyToContinue(ctx.console);
        
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
            if (!ConsoleUtils.getYesNo(ctx.console, "Continue? ")) return;
            
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
            
            ctx.console.println("\n");
            
        }
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
