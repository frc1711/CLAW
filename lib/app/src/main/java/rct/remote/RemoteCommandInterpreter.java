package rct.remote;

import rct.commands.Command;
import rct.commands.CommandInterpreter;
import rct.commands.CommandInterpreter.BadArgumentsException;

public class RemoteCommandInterpreter {
    
    private final CommandInterpreter interpreter = new CommandInterpreter();
    
    public RemoteCommandInterpreter () {
        addCommands();
    }
    
    private void addCommands () {
        
        // TODO: Standardize the help command/description and create a new message class so that a listing of commands can be sent from remote, so nonexistent commands can be caught even if there is no connection to remote
        
        interpreter.addCommandConsumer("ping", this::pingCommand);
        // interpreter.addCommandConsumer("help", this::helpCommand);
    }
    
    public boolean processLine (String line) throws Command.ParseException, BadArgumentsException {
        return interpreter.processLine(line);
    }
    
    private void pingCommand (Command cmd) {
        System.out.println("\n\npong\n\n");
    }
    
}
