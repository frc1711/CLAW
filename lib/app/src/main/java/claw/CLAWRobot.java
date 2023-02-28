package claw;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import claw.logs.CLAWLogger;
import claw.logs.LogHandler;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.network.low.Waiter;
import claw.rct.network.low.Waiter.NoValueReceivedException;
import claw.rct.remote.RCTServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class CLAWRobot {
    
    private static final Setting<Boolean> RUN_ROBOT_CODE_SETTING = new Setting<>("CLAW.RUN_ROBOT_CODE", () -> true);
    
    private static final CommandLineInterpreter EXTENSIBLE_COMMAND_INTERPRETER = new CommandLineInterpreter();
    
    private static final Waiter<Object> stopRuntimeThreadWaiter = new Waiter<>();
    private static boolean hasStartedCompetition = false;
    
    public static void startCompetition (TimedRobot robot, Runnable robotStartCompetition) {
        if (hasStartedCompetition)
            throw new RuntimeException("Cannot call startCompetition more than once");
        hasStartedCompetition = true;
        
        // Start the CLAW runtime if indicated by settings to do so and there is no FMS attached
        if (Preferences.getBoolean("RUN_CLAW_SERVER", true) && !DriverStation.isFMSAttached()) {
            startThread(CLAWRobot::initializeCLAWRuntime);
        }
        
        // Run robot code if indicated by settings to do so
        if (RUN_ROBOT_CODE_SETTING.get()) {
            startThread(() -> CLAWRobot.initializeRobotCode(robot, robotStartCompetition));
        }
        
        // Wait for stopRuntimeThreadWaiter so the robot runtime thread will wait until it is told to stop
        try {
            stopRuntimeThreadWaiter.waitForValue();
        } catch (NoValueReceivedException e) { }
        
        // Send any remaining data through the server, if one exists
        if (server != null)
            LogHandler.getInstance().sendData(server);
        
    }
    
    private static void startThread (Runnable thread) {
        new Thread(thread).start();
    }
    
    private static final CLAWLogger
        COMMANDS_LOG = CLAWLogger.getLogger("claw.commands"),
        RUNTIME_LOG = CLAWLogger.getLogger("claw.runtime");
    
    private static RCTServer server;
    
    private static void initializeRobotCode (TimedRobot robot, Runnable robotStartCompetition) {
        try {
            // Add the periodic method for the CLAWRobot and call start competition within a try-catch
            // loop to catch any exceptions
            robot.addPeriodic(CLAWRobot::robotPeriodic, TimedRobot.kDefaultPeriod);
            robotStartCompetition.run();
        } catch (Throwable exception) {
            // Catch any uncaught robot exceptions
            handleFatalUncaughtException(exception);
            throw exception;
        } finally {
            stopRuntimeThreadWaiter.kill();
        }
    }
    
    private static void initializeCLAWRuntime () {
        // Put a message into the console indicating that the CLAWRobot runtime has started
        System.out.println("\n -- CLAW is running -- \n");
        
        // Default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(CLAWRobot::handleUncaughtException);
        
        // Initialize command watchers
        CommandScheduler.getInstance().onCommandInitialize(CLAWRobot::onCommandInitialize);
        CommandScheduler.getInstance().onCommandExecute(CLAWRobot::onCommandExecute);
        CommandScheduler.getInstance().onCommandFinish(CLAWRobot::onCommandFinish);
        CommandScheduler.getInstance().onCommandInterrupt(CLAWRobot::onCommandInterrupt);
        
        // Start RCT server
        try {
            server = new RCTServer(5800, EXTENSIBLE_COMMAND_INTERPRETER);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start RCT server.");
            e.printStackTrace();
        }
    }
    
    public static CommandLineInterpreter getExtensibleCommandInterpreter () {
        return EXTENSIBLE_COMMAND_INTERPRETER;
    }
    
    public enum RuntimeMode {
        CLAW_SERVER_ONLY,
        CLAW_SERVER_AND_ROBOT_CODE,
    }
    
    public static void restartCode () {
        if (!hasStartedCompetition)
            throw new RuntimeException("Cannot restart code when startCompetition has not yet been called");
        stopRuntimeThreadWaiter.kill();
    }
    
    public static void restartCode (RuntimeMode mode) {
        if (!hasStartedCompetition)
            throw new RuntimeException("Cannot restart code when startCompetition has not yet been called");
        
        switch (mode) {
            case CLAW_SERVER_ONLY:
                RUN_ROBOT_CODE_SETTING.set(false);
                break;
            case CLAW_SERVER_AND_ROBOT_CODE:
                RUN_ROBOT_CODE_SETTING.set(true);
                break;
        }
        
        stopRuntimeThreadWaiter.kill();
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
    
    private static void handleFatalUncaughtException (Throwable exception) {
        // Put to the logger
        RUNTIME_LOG.err("Fatal uncaught exception in robot code:\n"+getStackTrace(exception));
    }
    
    private static String getStackTrace (Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
    
}
