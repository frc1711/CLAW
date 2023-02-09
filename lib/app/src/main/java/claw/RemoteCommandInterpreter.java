package claw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import claw.logs.LogHandler;
import claw.logs.LoggerDomain.InvalidLoggerDomainException;
import claw.rct.commands.Command;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.commands.CommandProcessor.CommandFunction;
import claw.rct.network.low.ConsoleManager;
import claw.testing.SystemsCheck;

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
        addCommand("syscheck", "syscheck", "syscheck", this::syscheckCommand);
        addCommand("inspect", "inspect", "inspect", this::inspectCommand);
        addCommand("watch",
            "watch [ --all | --none | log domain...]",
            "Use -a or --all to watch all logger domains. Use -n or --none to watch no logger domains.\n" +
            "Use 'watch [domain]...' to watch only a set of specific logger domains and all their subdomains.",
            this::watchCommand);
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        interpreter.addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
    }
    
    public void processLine (ConsoleManager console, String line)
            throws Command.ParseException, BadCallException, CommandNotRecognizedException {
        interpreter.processLine(console, line);
    }
    
    
    
    
    private void inspectCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowOptions("list");
        reader.allowFlags('l');
        
        Set<String> unitNames = UnitBuilder.getUnitNames();
        
        if (reader.getFlag('l') || reader.getOptionMarker("list")) {
            reader.noMoreArgs();
            
            for (String unitName : unitNames)
                console.println(unitName);
            
        } else {
            
            // Get the LiveUnit
            String unitName = reader.readArgOneOf("unit name", "The given unit name did not match any existing unit.", unitNames);
            LiveUnit unit = UnitBuilder.getUnitByName(unitName);
            if (unit == null)
                throw new BadCallException("The given unit '"+unitName+"' no longer exists.");
            
            // Repeatedly update the unit's live fields (until stopped by user input)
            int numFields = 0;
            while (!console.hasInputReady()) {
                console.moveUp(numFields);
                HashMap<String, String> fields = unit.getFields();
                numFields = fields.size();
                
                for (Entry<String, String> field : fields.entrySet()) {
                    console.clearLine();
                    console.println(field.getKey() + " : " + field.getValue());
                }
            }
            
        }
    }
    
    private void watchCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowOptions("all", "none");
        reader.allowFlags('a', 'n');
        
        if (reader.getFlag('a') || reader.getOptionMarker("all")) {
            
            // Watch all loggers
            LogHandler.getInstance().watchAllDomains();
            
        } else if (reader.getFlag('n') || reader.getOptionMarker("none")) {
            
            // Watch no loggers
            LogHandler.getInstance().unsetWatchedDomains();
            
        } else {
        
            // Unset all previous logger domains
            if (reader.hasNextArg())
                LogHandler.getInstance().unsetWatchedDomains();
            
            // Continue until there are no more arguments
            while (reader.hasNextArg()) {
                String domain = reader.readArgString("logger domain");
                
                try {
                    LogHandler.getInstance().watchDomain(domain);
                } catch (InvalidLoggerDomainException e) {
                    throw new BadCallException(e.getMessage());
                }
            }
            
        }
        
        // Get a sorted list of logger domains
        List<String> domainsList = new ArrayList<>();
        LogHandler.getInstance().getRegisteredDomains().forEach(domainsList::add);
        domainsList.sort(String::compareTo);
        
        // Print the list
        domainsList.forEach(domain -> {
            boolean watched = LogHandler.getInstance().isDomainWatched(domain);
            char watchedChar = watched ? '#' : ' ';
            console.println(watchedChar + " " + domain);
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
    
    private void syscheckCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        List<SystemsCheck> checks = SystemsCheck.getAllSystemsChecks();
        
        for (SystemsCheck check : checks) {
            console.println(check.getSystemName());
            console.flush();
            
            check.run(console);
            console.println("Testing testing 123");
        }
        
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