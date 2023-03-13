package claw;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import claw.logs.CLAWLogger;
import claw.logs.LogHandler;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.remote.RCTServer;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class CLAWRobot {
    
    // Runtime execution determined by preferences so that the user can control
    // this through any NetorkTables client (so that if you turn the server off,
    // you can still control this execution)
    private static final String RUN_RCT_SERVER = "CLAW.RUN_RCT_SERVER", RCT_SERVER_PORT = "CLAW.RCT_SERVER_PORT";
    private static final int DEFAULT_SERVER_PORT = 5800;
    
    private static final CommandLineInterpreter EXTENSIBLE_COMMAND_INTERPRETER = new CommandLineInterpreter();
    
    private static boolean hasStartedCompetition = false;
    
    public static void startCompetition (TimedRobot robot, Runnable robotStartCompetition) {
        // Do not call startCompetition more than once
        if (hasStartedCompetition)
            throw new RuntimeException("Cannot call startCompetition more than once");
        hasStartedCompetition = true;
        
        // Start the RCT server if indicated by preferences to do so
        Preferences.initBoolean(RUN_RCT_SERVER, true);
        Preferences.initInt(RCT_SERVER_PORT, DEFAULT_SERVER_PORT);
        if (Preferences.getBoolean(RUN_RCT_SERVER, true)) {
            startThread(CLAWRobot::initializeRCTServer);
        }
        
        // Run until robot code finishes
        runRobotCode(robot, robotStartCompetition);
        
    }

    private static void startThread (Runnable thread) {
        new Thread(thread).start();
    }

    private static final CLAWLogger
        COMMANDS_LOG = CLAWLogger.getLogger("claw.commands"),
        RUNTIME_LOG = CLAWLogger.getLogger("claw.runtime");

    private static RCTServer server;
    
    /**
     * Start the robot code and the CLAW robot code runtime necessary for robot code functioning
     */
    private static void runRobotCode (TimedRobot robot, Runnable robotStartCompetition) {
        // Put a message into the console indicating that the CLAWRobot runtime has started
        System.out.println("\n -- CLAW is running -- \n");
        
        // Default uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(CLAWRobot::handleUncaughtException);
        
        // Initialize command watchers
        CommandScheduler.getInstance().onCommandInitialize(CLAWRobot::onCommandInitialize);
        CommandScheduler.getInstance().onCommandExecute(CLAWRobot::onCommandExecute);
        CommandScheduler.getInstance().onCommandFinish(CLAWRobot::onCommandFinish);
        CommandScheduler.getInstance().onCommandInterrupt(CLAWRobot::onCommandInterrupt);
        
        try {
            // Add the periodic method for the CLAWRobot and call start competition within a try-catch
            // loop to catch any exceptions
            robot.addPeriodic(CLAWRobot::robotPeriodic, TimedRobot.kDefaultPeriod);
            robotStartCompetition.run();
        } catch (Throwable exception) {
            // Catch any uncaught robot exceptions
            handleFatalUncaughtException(exception);
            throw exception;
        }
    }
    
    /**
     * Initialize the RCT server allowing for advanced debugging and control from the driver station
     */
    private static void initializeRCTServer () {
        // Start RCT server
        try {
            server = new RCTServer(Preferences.getInt(RCT_SERVER_PORT, DEFAULT_SERVER_PORT), EXTENSIBLE_COMMAND_INTERPRETER);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start RCT server.");
            e.printStackTrace();
        }
    }
    
    public static CommandLineInterpreter getExtensibleCommandInterpreter () {
        return EXTENSIBLE_COMMAND_INTERPRETER;
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
        System.err.println(
            "Caught an exception in the robot code: " + exception.getMessage()+". Use the " + 
            "'errlog' command in the Robot Control Terminal to examine it."
        );
        
        RobotErrorLog.logThreadError(exception);
        
        // Put to the logger
        RUNTIME_LOG.err("Uncaught exception in a thread '"+thread.getName()+"':\n"+getStackTrace(exception));
    }
    
    private static void handleFatalUncaughtException (Throwable exception) {
        // Put to the logger
        RUNTIME_LOG.err("Fatal uncaught exception in robot code:\n"+getStackTrace(exception));
        
        RobotErrorLog.logFatalError(exception);
    }
    
    private static String getStackTrace (Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
    
}
