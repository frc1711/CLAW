package claw;

import java.util.ArrayList;
import java.util.List;

import claw.logs.LogHandler;
import claw.rct.commands.Command;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.commands.CommandProcessor.CommandFunction;
import claw.rct.network.low.ConsoleManager;

/**
 * This class is meant for CLAW's internal use only.
 */
public class RemoteCommandInterpreter {
    
    private final CommandLineInterpreter interpreter = new CommandLineInterpreter();
    
    public RemoteCommandInterpreter () {
        addCommands();
    }
    
    private void addCommands () {
        
        // TODO: Standardize the help command/description and create a new message class so that a listing of commands can be sent from remote, so nonexistent commands can be caught even if there is no connection to remote
        
        addCommand("ping", "[ping usage]", "[ping help]", this::pingCommand);
        addCommand("test", "[test usage]", "[test help]", this::testCommand);
        addCommand("config", "config", "config", this::configCommand);
        addCommand("watch",
            "watch [ --all | --none | log name...]",
            "Use -a or --all to watch all logs. Use -n or --none to watch no logs.\n" +
            "Use 'watch [name]...' to watch only a set of specific logs.",
            this::watchCommand);
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        interpreter.addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
    }
    
    public void processLine (ConsoleManager console, String line)
            throws Command.ParseException, BadCallException, CommandNotRecognizedException {
        try {
            interpreter.processLine(console, line);
        } catch (CommandNotRecognizedException e) {
            CLAWRobot.getExtensibleCommandInterpreter().processLine(console, line);
        }
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
    
    private void pingCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        
        console.println("pong");
        String input = console.readInputLine();
        console.printlnSys("Read input line: " + input);
    }
    
    private void configCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        
        
        // TODO: Fix config command
        
        // Map<String, String> fields = Config.getInstance().getFields();
        // for (Entry<String, String> field : fields.entrySet()) {
        //     String limitKey = field.getKey();
        //     if (limitKey.length() > 25)
        //         limitKey = limitKey.substring(0, 25) + "...";
            
        //     console.println(limitKey + " : " + " ".repeat(30 - limitKey.length()) + field.getValue());
        // }
        
        // console.println(fields.size() + " fields");
    }
    
    private void testCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        int number = 0;
        
        console.println("");
        while (!console.hasInputReady()) {
            console.moveUp(1);
            number ++;
            console.printlnSys(""+number);
        }
    }
    
}
