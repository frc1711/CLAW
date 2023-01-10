package claw.internal.rct.remote;

import java.util.List;

import claw.CLAWRobot;
import claw.internal.Registry;
import claw.internal.rct.commands.Command;
import claw.internal.rct.commands.CommandLineInterpreter;
import claw.internal.rct.commands.CommandProcessor;
import claw.internal.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.internal.rct.commands.CommandProcessor.BadArgumentsException;
import claw.internal.rct.commands.CommandProcessor.CommandFunction;
import claw.internal.rct.network.low.ConsoleManager;
import claw.api.subsystems.SubsystemCLAW;

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
        addCommand("restart", "[restart usage]", "[restart help]", this::restartCommand);
        addCommand("subsystems", "[subsystems usage]", "[subsystems help]", this::subsystemsCommand);
        addCommand("config", "config", "config", this::configCommand);
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        interpreter.addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
    }
    
    public void processLine (ConsoleManager console, String line)
            throws Command.ParseException, BadArgumentsException, CommandNotRecognizedException {
        interpreter.processLine(console, line);
    }
    
    
    
    private void pingCommand (ConsoleManager console, Command cmd) {
        console.println("pong");
        String input = console.readInputLine();
        console.printlnSys("Read input line: " + input);
    }
    
    private void restartCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.expectNothing(cmd);
        
        console.println("Restarting...");
        console.flush();
        
        CLAWRobot.getInstance().restartCode();
    }
    
    private void subsystemsCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.expectNoOptions(cmd);
        CommandProcessor.expectNoFlags(cmd);
        String firstArg = CommandProcessor.expectString(cmd, "operation", 0);
        
        // TODO: Migrate functionality to new status and config commands (status subsystem SubsystemName, config subsystem SubsystemName)
        
        if (firstArg.equals("list")) {
            
            List<String> subsystemNames = subsystemRegistry.getItemNames();
            if (subsystemNames.size() == 0) {
                console.println("No CLAW subsystems were found.");
            } else {
                for (String name : subsystemNames)
                    console.println(name);
                console.println("count: " + subsystemNames.size());
            }
            
        } else {
            String subsystemName = CommandProcessor.expectOneOf(cmd, "subsystem name", 0, subsystemRegistry.getItemNames());
            
            SubsystemCLAW subsystem = subsystemRegistry.getItem(subsystemName);
            RCTSendableBuilder builder = new RCTSendableBuilder(console, subsystem);
            subsystem.initSendable(builder);
            
            String[] lines = builder.getFieldsDisplay();
            for (String line : lines)
                console.println(line);
        }
        
    }
    
    private void configCommand (ConsoleManager console, Command cmd) {
        
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
    
    private void testCommand (ConsoleManager console, Command cmd) {
        int number = 0;
        
        console.println("");
        while (!console.hasInputReady()) {
            console.moveUp(1);
            number ++;
            console.printlnSys(""+number);
        }
    }
    
}
