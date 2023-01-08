package claw.rct.remote;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import claw.CLAWRuntime;
import claw.Config;
import claw.Registry;
import claw.CLAWRuntime.RobotMode;
import claw.rct.commands.Command;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.rct.commands.CommandProcessor.BadArgumentsException;
import claw.rct.commands.CommandProcessor.CommandFunction;
import claw.rct.network.low.ConsoleManager;
import claw.subsystems.SubsystemCLAW;

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
        CommandProcessor.checkNumArgs(0, 1, cmd.argsLen());
        
        RobotMode restartMode = RobotMode.DEFAULT;
        
        if (cmd.argsLen() == 1) {
            
            // Restart mode passed in as an argument
            CommandProcessor.expectedOneOf("restart mode", cmd.getArg(0), "default", "sysconfig");
            
            if (cmd.getArg(0).equals("sysconfig")) {
                restartMode = RobotMode.SYSCONFIG;
            }
            
        }
        
        console.println("Restarting...");
        
        CLAWRuntime.getInstance().restartCode(restartMode);
    }
    
    private void subsystemsCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.checkNumArgs(1, 3, cmd.argsLen());
        
        String firstArg = cmd.getArg(0);
        
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
            
        }
        
    }
    
    private void configCommand (ConsoleManager console, Command cmd) {
        Map<String, String> fields = Config.getInstance().getFields();
        for (Entry<String, String> field : fields.entrySet()) {
            String limitKey = field.getKey();
            if (limitKey.length() > 25)
                limitKey = limitKey.substring(0, 25) + "...";
            
            console.println(limitKey + " : " + " ".repeat(30 - limitKey.length()) + field.getValue());
        }
        
        console.println(fields.size() + " fields");
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
