package rct.remote;

import rct.commands.Command;
import rct.commands.CommandInterpreter;
import rct.commands.CommandInterpreter.BadArgumentsException;
import rct.network.low.ConsoleManager;

public class RemoteCommandInterpreter {
    
    private final CommandInterpreter interpreter = new CommandInterpreter();
    
    public RemoteCommandInterpreter () {
        addCommands();
    }
    
    private void addCommands () {
        
        // TODO: Standardize the help command/description and create a new message class so that a listing of commands can be sent from remote, so nonexistent commands can be caught even if there is no connection to remote
        
        interpreter.addCommandConsumer("ping", this::pingCommand);
        interpreter.addCommandConsumer("test", this::testCommand);
        // interpreter.addCommandConsumer("help", this::helpCommand);
    }
    
    public boolean processLine (ConsoleManager console, String line) throws Command.ParseException, BadArgumentsException {
        return interpreter.processLine(console, line);
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
