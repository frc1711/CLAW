package rct.local;

import rct.commands.Command;
import rct.commands.CommandInterpreter;

public class LocalCommandInterpreter extends CommandInterpreter {
    
    private final ConsoleManager console;
    private final StreamDataStorage streamDataStorage;
    
    public LocalCommandInterpreter (ConsoleManager console, StreamDataStorage streamDataStorage) {
        this.console = console;
        this.streamDataStorage = streamDataStorage;
        addCommands();
    }
    
    private void addCommands () {
        addCommandConsumer("local", this::localCommand);
    }
    
    public void localCommand (Command cmd) {
        console.println("This is the 'local' command, executed locally instead of on the roboRIO.");
        console.println("Num args passed in: " + cmd.argsLen());
    }
    
}
