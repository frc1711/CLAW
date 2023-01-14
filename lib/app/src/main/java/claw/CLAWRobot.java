package claw;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

import claw.internal.Config;
import claw.internal.Registry;
import claw.internal.Config.ConfigField;
import claw.internal.Registry.NameConflictException;
import claw.internal.logs.LogHandler;
import claw.api.CLAWLogger;
import claw.internal.rct.remote.RCTServer;
import claw.api.subsystems.SubsystemCLAW;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class CLAWRobot {
    
    // Logs
    
    private static final CLAWLogger
        SUBSYSTEM_LOG = CLAWLogger.getLogger("claw.subsystems"),
        COMMANDS_LOG = CLAWLogger.getLogger("claw.commands"),
        ROBOT_LOG = CLAWLogger.getLogger("claw.robot");
    
    // Config
    
    private static final File CONFIG_FILE = new File("/home/lvuser/claw-config.ser");
    private static final Config CONFIG = new Config(CONFIG_FILE);
    
    private static final ConfigField<String> UNCAUGHT_EXCEPTION_FIELD = CONFIG.getField("UNCAUGHT_EXCEPTION");
    
    
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
                return instance.robotProxy;
            }
        };
    }
    
    
    
    // CLAWRobot private methods
    
    private final Registry<SubsystemCLAW> subsystemRegistry = new Registry<>("subsystem");
    private final RobotProxy robotProxy;
    private RCTServer server;
    
    private CLAWRobot (Supplier<TimedRobot> robotSupplier) {
        // Put a message into the console indicating that the CLAWRobot runtime has started
        System.out.println("\n -- CLAWRobot is running -- \n");
        
        // Initialize the robot proxy
        robotProxy = new RobotProxy(robotSupplier);
        
        // Default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
        
        // Send the last uncaught exception
        String uncaughtException = UNCAUGHT_EXCEPTION_FIELD.getValue(null);
        UNCAUGHT_EXCEPTION_FIELD.setValue(null);
        if (uncaughtException != null)
            ROBOT_LOG.err("Uncaught exception from last execution:\n" + uncaughtException);
        
        // Start the RCT server in another thread (so that the server startup is non-blocking)
        new Thread(() -> {
            try {
                server = new RCTServer(5800, subsystemRegistry);
                server.start();
            } catch (IOException e) {
                System.err.println("Failed to start RCT server.");
                e.printStackTrace();
            }
        }).start();
    }
    
    private String getStackTrace (Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
    
    private void handleUncaughtException (Thread thread, Throwable e) {
        // Print to the driver station
        System.err.println("Caught an uncaught exception: " + e.getMessage());
        
        // Put to the logger
        ROBOT_LOG.err("Uncaught exception in a thread '"+thread.getName()+"':\n"+getStackTrace(e));
    }
    
    private void handleFatalUncaughtException (Throwable e) {
        // Try printing to the driver station
        System.err.println("Caught a fatal uncaught exception: " + e.getMessage());
        
        // Put the stack trace to the uncaught exception field
        UNCAUGHT_EXCEPTION_FIELD.setValue(getStackTrace(e));
    }
    
    /**
     * The robot proxy schedules this method to be called at the default TimedRobot period
     */
    private void robotPeriodic () {
        if (server != null)
            LogHandler.getInstance().sendData(server);
    }
    
    /**
     * The robot proxy calls this method before the startCompetition method exits (regardless
     * of whether any exceptions have been thrown) so that important operations can be finished.
     */
    private void onRobotProgramExit () {
        CONFIG.save();
        ROBOT_LOG.out("Exiting robot program");
        LogHandler.getInstance().sendData(server);
    }
    
    private void onCommandInitialize (Command command) {
        COMMANDS_LOG.out(command.getName() + " initialized");
    }
    
    private void onCommandExecute (Command command) {
        
    }
    
    private void onCommandFinish (Command command) {
        COMMANDS_LOG.out(command.getName() + " finished");
    }
    
    private void onCommandInterrupt (Command command) {
        COMMANDS_LOG.out(command.getName() + " was interrupted");
    }
    
    
    
    // Public API
    
    public void addSubsystem (SubsystemCLAW subsystem) {
        try {
            subsystemRegistry.add(subsystem.getName(), subsystem);
        } catch (NameConflictException e) {
            SUBSYSTEM_LOG.out("Warning: " + e.getMessage());
        }
    }
    
    public void restartCode () {
        onRobotProgramExit();
        System.exit(0);
    }
    
    
    
    // RobotProxy instance methods
    
    private class RobotProxy extends RobotBase {
        
        private final TimedRobot robot;
        
        public RobotProxy (Supplier<TimedRobot> robotSupplier) {
            ROBOT_LOG.out("Starting robot code");
            robot = robotSupplier.get();
            
            // Schedule the robotPeriodic method to be called at the default TimedRobot period
            robot.addPeriodic(CLAWRobot.this::robotPeriodic, TimedRobot.kDefaultPeriod);
            
            CommandScheduler.getInstance().onCommandInitialize(CLAWRobot.this::onCommandInitialize);
            CommandScheduler.getInstance().onCommandExecute(CLAWRobot.this::onCommandExecute);
            CommandScheduler.getInstance().onCommandFinish(CLAWRobot.this::onCommandFinish);
            CommandScheduler.getInstance().onCommandInterrupt(CLAWRobot.this::onCommandInterrupt);
        }
        
        @Override
        public void startCompetition () {
            try {
                
                // Call the main robot's startCompetition method
                robot.startCompetition();
                
                // Handle robot program exiting
                onRobotProgramExit();
            } catch (Throwable throwable) {
                handleFatalUncaughtException(throwable);
                onRobotProgramExit();
                throw throwable;
            }
        }
        
        @Override
        public void endCompetition () {
            robot.endCompetition();
        }
        
    }
    
}
