package claw;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

import claw.logs.LogHandler;
import claw.rct.remote.RCTServer;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class CLAWRobot {
    
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
                TimedRobot robot = robotSupplier.get();
                initializeRuntime();
                robot.addPeriodic(CLAWRobot::robotPeriodic, TimedRobot.kDefaultPeriod);
                return robot;
            }
        };
    }
    
    private static final CLAWLogger
        COMMANDS_LOG = CLAWLogger.getLogger("claw.commands"),
        RUNTIME_LOG = CLAWLogger.getLogger("claw.runtime");
    
    private static RCTServer server;
    private static boolean initialized = false;
    
    private static void initializeRuntime () {
        
        if (initialized) return;
        initialized = true;
        
        // Put a message into the console indicating that the CLAWRobot runtime has started
        System.out.println("\n -- CLAW is running -- \n");
        
        // Default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(CLAWRobot::handleUncaughtException);
        
        // Initialize command watchers
        CommandScheduler.getInstance().onCommandInitialize(CLAWRobot::onCommandInitialize);
        CommandScheduler.getInstance().onCommandExecute(CLAWRobot::onCommandExecute);
        CommandScheduler.getInstance().onCommandFinish(CLAWRobot::onCommandFinish);
        CommandScheduler.getInstance().onCommandInterrupt(CLAWRobot::onCommandInterrupt);
        
        // Start RCT server thread
        new Thread(() -> {
            try {
                server = new RCTServer(5800);
                server.start();
            } catch (IOException e) {
                System.err.println("Failed to start RCT server.");
                e.printStackTrace();
            }
        }).start();
        
    }
    
    
    private static void robotPeriodic () {
        if (server != null)
            LogHandler.getInstance().sendData(server);
    }
    
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
    
    private static String getStackTrace (Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
    
}
