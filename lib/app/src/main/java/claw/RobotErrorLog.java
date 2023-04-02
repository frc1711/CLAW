package claw;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import claw.actions.compositions.Context.TerminatedContextException;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.network.low.ConsoleManager;

public class RobotErrorLog {
    
    private static record LoggableError (long timestamp, String text, ErrorType type) implements Serializable { }
    
    public static final CommandProcessor ERROR_LOG_COMMAND_PROCESSOR = new CommandProcessor(
        "errlog",
        "errlog",
        "Use 'errlog' to view logged robot errors and warnings through the RobotErrorLog class.",
        RobotErrorLog::errorLogCommand
    );
    
    private static final Setting<ArrayList<LoggableError>> ERROR_LOG_SETTING = new Setting<>("CLAW.ERROR_LOG", () -> new ArrayList<>());
    private static final Object ERROR_LOG_LOCK = new Object();
    
    private static void errorLogCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        // Copy an array of errors from the error log setting
        LoggableError[] errors;
        synchronized (ERROR_LOG_LOCK) {
            errors = ERROR_LOG_SETTING.get().toArray(new LoggableError[0]);
        }
        
        if (errors.length > 0) {
            console.println("There are "+errors.length+" logged error(s).");
            
            for (LoggableError error : errors) {
                console.println(" -- " + error.type.name + " -- ");
                console.println(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date(error.timestamp)));
                
                String text = ConsoleManager.formatMessage(error.text);
                
                switch (error.type) {
                    case FATAL_ERROR:
                    case THREAD_ERROR:
                        console.printlnErr(text);
                        break;
                    case WARNING:
                        console.printlnSys(text);
                        break;
                }
                
                console.println("\n");
                
            }
            
        } else {
            console.println("There are no logged errors.");
        }
    }
    
    private static void logError (LoggableError error) {
        // TODO: Based on robot tests, this takes waaay too long. Could indicate settings take too long to read from. More testing required.
        synchronized (ERROR_LOG_LOCK) {
            ERROR_LOG_SETTING.get().add(error);
            ERROR_LOG_SETTING.save();
        }
    }
    
    /**
     * Log a fatal error (an error which kills the robot code). This method for logging an error
     * should only be used internally in CLAW.
     * @param exception The exception to log.
     */
    public static void logFatalError (Throwable exception) {
        logError(new LoggableError(System.currentTimeMillis(), getStackTrace(exception), ErrorType.FATAL_ERROR));
    }
    
    /**
     * Log an error which kills some thread of the robot code. This method for logging an error
     * should only be used internally in CLAW.
     * @param exception The exception to log.
     */
    public static void logThreadError (Throwable exception) {
        logError(new LoggableError(System.currentTimeMillis(), getStackTrace(exception), ErrorType.THREAD_ERROR));
    }
    
    /**
     * Log a warning with a given description.
     * @param description   The warning's description.
     */
    public static void logWarning (String description) {
        logError(new LoggableError(System.currentTimeMillis(), description, ErrorType.WARNING));
    }
    
    private static String getStackTrace (Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
    
    private enum ErrorType {
        FATAL_ERROR     ("Fatal Error"),
        THREAD_ERROR    ("Thread Error"),
        WARNING         ("Warning");
        
        private final String name;
        private ErrorType (String name) {
            this.name = name;
        }
    }
    
}
