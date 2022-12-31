package rct.remote;

import rct.commands.Command;
import rct.commands.CommandLineInterpreter;
import rct.commands.CommandProcessor;
import rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import rct.commands.CommandProcessor.BadArgumentsException;
import rct.commands.CommandProcessor.CommandFunction;
import rct.network.low.ConsoleManager;

public class RemoteCommandInterpreter {
    
    private final CommandLineInterpreter interpreter = new CommandLineInterpreter();
    
    public RemoteCommandInterpreter () {
        addCommands();
    }
    
    private void addCommands () {
        
        // TODO: Standardize the help command/description and create a new message class so that a listing of commands can be sent from remote, so nonexistent commands can be caught even if there is no connection to remote
        
        addCommand("ping", "[ping usage]", "[ping help]", this::pingCommand);
        addCommand("test", "[test usage]", "[test help]", this::testCommand);
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
