package claw.rct.remote;

import java.util.ArrayList;
import java.util.List;

import claw.logs.LogHandler;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.commands.CommandProcessor.CommandFunction;
import claw.rct.network.low.ConsoleManager;
import claw.subsystems.CLAWSubsystem;

/**
 * This class is meant for CLAW's internal use only.
 */
public class RemoteCommandInterpreter extends CommandLineInterpreter {
    
    public RemoteCommandInterpreter () {
        addCommands();
    }
    
    private void addCommands () {
        addCommand("watch",
            "watch [ --all | --none | log name...]",
            "Use -a or --all to watch all logs. Use -n or --none to watch no logs. " +
            "Use 'watch [name]...' to watch only a set of specific logs.",
            this::watchCommand);
        addCommandProcessor(CLAWSubsystem.COMMAND_PROCESSOR);
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
    }
    
    private void watchCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowOptions("all", "none");
        reader.allowFlags('a', 'n');
        
        if (reader.getFlag('a') || reader.getOptionMarker("all")) {
            
            // Watch all logs
            LogHandler.getInstance().watchAllLogs();
            
        } else if (reader.getFlag('n') || reader.getOptionMarker("none")) {
            
            // Watch no logs
            LogHandler.getInstance().stopWatchingLogs();
            
        } else {
        
            // Unset all previous log names
            if (reader.hasNextArg())
                LogHandler.getInstance().stopWatchingLogs();
            
            // Continue until there are no more arguments
            while (reader.hasNextArg()) {
                String logName = reader.readArgString("logger name");
                LogHandler.getInstance().watchLogName(logName);
            }
            
        }
        
        // Get a sorted list of log names
        List<String> logNamesList = new ArrayList<>();
        LogHandler.getInstance().getRegisteredLogNames().forEach(logNamesList::add);
        logNamesList.sort(String::compareTo);
        
        // Print the list
        logNamesList.forEach(logName -> {
            boolean watched = LogHandler.getInstance().isWatchingLog(logName);
            char watchedChar = watched ? '#' : ' ';
            console.println(watchedChar + " " + logName);
        });
    }
    
}
