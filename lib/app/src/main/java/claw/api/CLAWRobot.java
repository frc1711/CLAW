package claw.api;

import java.util.function.Supplier;

import claw.api.CLAWSettings.Setting;
import claw.internal.CLAWRuntime;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;

public class CLAWRobot extends RobotBase {
    
    private static final CLAWLogger ROBOT_LOG = CLAWLogger.getLogger("claw.robot");
    private static final Setting<String> UNCAUGHT_EXCEPTION = CLAWSettings.getStringSetting("claw.uncaughtException");
    
    /**
     * Get a {@code Supplier<RobotBase>} that provides a {@link RobotBase} proxy which CLAW can use. This robot proxy
     * will also start all necessary CLAW processes. This method should only be used in {@code Main.java} as a wrapper
     * around {@code Robot::new}.
     * @param robotSupplier A {@code Supplier<TimedRobot>} which can be used to get a new robot object.
     * @return              The {@code Supplier<RobotBase>} containing the CLAW robot proxy.
     */
    public static Supplier<RobotBase> fromRobot (Supplier<TimedRobot> robotSupplier) {
        return new Supplier<RobotBase>(){
            @Override
            public RobotBase get () {
                
                // When this supplier is called in RobotBase.startRobot, it will initialize the CLAWRobot instance
                if (instance == null)
                    instance = new CLAWRobot(robotSupplier);
                return instance;
            }
        };
    }
    
    
    // CLAWRobot singleton initialization with fromRobot (called in Main.java) and retrieval with getInstance
    
    private static CLAWRobot instance = null;
    
    public static CLAWRobot getInstance () {
        // This runtime exception should never ever happen, as there is practically zero possibility
        // of anyone trying to access a CLAWRobot instance before it is initialized by the
        // fromRobot supplier sent to RobotBase.startRobot in Main.java.
        
        // The only case in which this can reasonably happen is if the CLAWRobot entry point
        // has not yet been added to Main.java
        if (instance == null)
            throw new RuntimeException("CLAWRobot has not been initialized in Main.java");
        return instance;
    }
    
    
    
    // CLAWRobot private methods
    
    private final TimedRobot userRobot;
    
    private CLAWRobot (Supplier<TimedRobot> robotSupplier) {
        // Initialize the CLAWRuntime so that runtime methods can be called
        CLAWRuntime.initialize();
        
        // Send the last uncaught exception
        String uncaughtExceptionString = UNCAUGHT_EXCEPTION.getValue(null);
        if (uncaughtExceptionString != null) {
            ROBOT_LOG.err("Uncaught exception from last execution:\n" + uncaughtExceptionString);
            UNCAUGHT_EXCEPTION.setValue(null);
            CLAWSettings.save();
        }
        
        // Get the user robot and add the runtime periodic
        userRobot = robotSupplier.get();
        userRobot.addPeriodic((CLAWRuntime.getInstance())::robotPeriodic, TimedRobot.kDefaultPeriod);
    }
    
    @Override
    public void startCompetition () {
        try {
            // Call the main robot's startCompetition method
            userRobot.startCompetition();
            
            // Handle robot program exiting
            CLAWRuntime.getInstance().onRobotProgramExit();
        } catch (Throwable throwable) {
            handleFatalUncaughtException(throwable);
            CLAWRuntime.getInstance().onRobotProgramExit();
            throw throwable;
        }
    }
    
    @Override
    public void endCompetition () {
        userRobot.endCompetition();
    }
    
    public static void restartCode () {
        CLAWRuntime.getInstance().restartCode();
    }
    
    private static void handleFatalUncaughtException (Throwable e) {
        // Try printing to the driver station
        System.err.println("Caught a fatal uncaught exception: " + e.getMessage());
        
        // Put the stack trace to the uncaught exception field
        UNCAUGHT_EXCEPTION.setValue(CLAWRuntime.getStackTrace(e));
    }
    
}
