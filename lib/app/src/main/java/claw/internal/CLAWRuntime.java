package claw.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import claw.api.CLAWLogger;
import claw.api.CLAWSettings;
import claw.api.subsystems.SubsystemCLAW;
import claw.internal.Registry.NameConflictException;
import claw.internal.logs.LogHandler;
import claw.internal.rct.remote.RCTServer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class CLAWRuntime {
    
    // Static
    
    // Logs
    
    private static final CLAWLogger
        SUBSYSTEM_LOG = CLAWLogger.getLogger("claw.subsystems"),
        COMMANDS_LOG = CLAWLogger.getLogger("claw.commands"),
        RUNTIME_LOG = CLAWLogger.getLogger("claw.runtime");
    
    // Config
    
    private static CLAWRuntime instance;
    
    public static void initialize () {
        if (instance != null)
            instance = new CLAWRuntime();
    }
    
    public static CLAWRuntime getInstance () {
        if (instance == null)
            instance = new CLAWRuntime();
        return instance;
    }
    
    
    
    
    // Instance
    
    private final Registry<SubsystemCLAW> subsystemRegistry = new Registry<>("subsystem");
    
    private RCTServer server;
    
    private CLAWRuntime () {
        
        // Put a message into the console indicating that the CLAWRobot runtime has started
        System.out.println("\n -- CLAW is running -- \n");
        
        // Default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(CLAWRuntime::handleUncaughtException);
        
        // Initialize command watchers
        CommandScheduler.getInstance().onCommandInitialize(CLAWRuntime::onCommandInitialize);
        CommandScheduler.getInstance().onCommandExecute(CLAWRuntime::onCommandExecute);
        CommandScheduler.getInstance().onCommandFinish(CLAWRuntime::onCommandFinish);
        CommandScheduler.getInstance().onCommandInterrupt(CLAWRuntime::onCommandInterrupt);
        
        // Start RCT server thread
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
    
    
    // Exposed methods
    
    public void robotPeriodic () {
        if (server != null)
            LogHandler.getInstance().sendData(server);
    }
    
    public void restartCode () {
        onRobotProgramExit();
        System.exit(0);
    }
    
    /**
     * This method should be called before the robot program exits (if you call System.exit() or if the robot program otherwise
     * somehow quits). This method call is not necessary if {@link CLAWRuntime#restartCode()} is used.
     */
    public void onRobotProgramExit () {
        CLAWSettings.save();
        RUNTIME_LOG.out("Exiting robot program");
        LogHandler.getInstance().sendData(server);
    }
    
    public void addSubsystem (SubsystemCLAW subsystem) {
        try {
            subsystemRegistry.add(subsystem.getName(), subsystem);
        } catch (NameConflictException e) {
            SUBSYSTEM_LOG.out("Warning: " + e.getMessage());
        }
    }
    
    
    
    // Private methods for handling events
    
    private static void onCommandInitialize (Command command) {
        COMMANDS_LOG.out(command.getName() + " initialized");
    }
    
    private static void onCommandExecute (Command command) {
        // Nothing here
    }
    
    private static void onCommandFinish (Command command) {
        COMMANDS_LOG.out(command.getName() + " finished");
    }
    
    private static void onCommandInterrupt (Command command) {
        COMMANDS_LOG.out(command.getName() + " was interrupted");
    }
    
    private static void handleUncaughtException (Thread thread, Throwable exception) {
        // Print to the driver station
        System.err.println("Caught an uncaught exception: " + exception.getMessage());
        
        // Put to the logger
        RUNTIME_LOG.err("Uncaught exception in a thread '"+thread.getName()+"':\n"+getStackTrace(exception));
    }
    
    
    // Utilities
    
    public static String getStackTrace (Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
    
}
