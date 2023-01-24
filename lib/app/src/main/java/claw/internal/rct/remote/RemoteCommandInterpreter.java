package claw.internal.rct.remote;

import java.util.ArrayList;
import java.util.List;

import claw.internal.Registry;
import claw.internal.logs.LogHandler;
import claw.internal.logs.LoggerDomain.InvalidLoggerDomainException;
import claw.internal.rct.commands.Command;
import claw.internal.rct.commands.CommandLineInterpreter;
import claw.internal.rct.commands.CommandProcessor;
import claw.internal.rct.commands.CommandReader;
import claw.internal.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.internal.rct.commands.CommandProcessor.BadCallException;
import claw.internal.rct.commands.CommandProcessor.CommandFunction;
import claw.internal.rct.network.low.ConsoleManager;
import claw.LiveUnit;
import claw.SubsystemCLAW;
import claw.UnitBuilder;

public class RemoteCommandInterpreter {
    
    private final CommandLineInterpreter interpreter = new CommandLineInterpreter();
    
    private final Registry<SubsystemCLAW> subsystemRegistry;
    
    public RemoteCommandInterpreter (Registry<SubsystemCLAW> subsystemRegistry) {
        this.subsystemRegistry = subsystemRegistry;
        addCommands();
    }
    
    private void addCommands () {
        
        // TODO: Standardize the help command/description and create a new message class so that a listing of commands can be sent from remote, so nonexistent commands can be caught even if there is no connection to remote
        
        addCommand("ping", "[ping usage]", "[ping help]", this::pingCommand);
        addCommand("test", "[test usage]", "[test help]", this::testCommand);
        addCommand("subsystems", "[subsystems usage]", "[subsystems help]", this::subsystemsCommand);
        addCommand("config", "config", "config", this::configCommand);
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
        
        List<String> unitNames = UnitBuilder.getUnitNames();
        
        if (reader.getFlag('l') || reader.getOptionMarker("list")) {
            reader.noMoreArgs();
            
            for (String unitName : unitNames)
                console.println(unitName);
            
        } else {
            
            String unitName = reader.readArgOneOf("unit name", "The given unit name did not match any existing unit.", unitNames);
            LiveUnit unit = UnitBuilder.getUnitByName(unitName);
            if (unit == null)
                throw new BadCallException("The given unit '"+unitName+"' no longer exists.");
            
            console.println("The unit "+unitName+" exists.");
            
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
    
    private void subsystemsCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNoOptions();
        reader.allowNoFlags();
        
        String operation = reader.readArgOneOf("operation", "Operation must be 'list' or 'get'.", "list", "get");
        
        // TODO: Migrate functionality to new status and config commands (status subsystem SubsystemName, config subsystem SubsystemName)
        
        if (operation.equals("list")) {
            
            List<String> subsystemNames = subsystemRegistry.getItemNames();
            if (subsystemNames.size() == 0) {
                console.println("No CLAW subsystems were found.");
            } else {
                for (String name : subsystemNames)
                    console.println(name);
                console.println("count: " + subsystemNames.size());
            }
            
        } else {
            
            // // String subsystemName = CommandProcessor.expectOneOf(cmd, "subsystem name", 0, subsystemRegistry.getItemNames());
            
            // SubsystemCLAW subsystem = subsystemRegistry.getItem(subsystemName);
            // RCTSendableBuilder builder = new RCTSendableBuilder(console, subsystem);
            // subsystem.initSendable(builder);
            
            // String[] lines = builder.getFieldsDisplay();
            // for (String line : lines)
            //     console.println(line);
        }
        
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
