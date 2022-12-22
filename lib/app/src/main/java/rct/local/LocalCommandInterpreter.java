package rct.local;

import java.io.IOException;

import rct.commands.Command;
import rct.commands.CommandInterpreter;
import rct.local.LocalSystem.ConnectionStatus;
import rct.network.low.DriverStationSocketHandler;

public class LocalCommandInterpreter extends CommandInterpreter {
    
    private final ConsoleManager console;
    private final StreamDataStorage streamDataStorage;
    private final LocalSystem system;
    
    /**
     * Because commands are sent to remote when the local command interpreter indicates that it does not recognize a command,
     * this sendCommandToRemote boolean can be set in a command consumer method in order to indicate to the processLine method
     * that it should indicate it does not recognize the command and thus must send the command to remote.
     */
    private boolean sendCommandToRemote = false;
    
    public LocalCommandInterpreter (ConsoleManager console, LocalSystem system, StreamDataStorage streamDataStorage) {
        this.console = console;
        this.streamDataStorage = streamDataStorage;
        this.system = system;
        addCommands();
    }
    
    private void addCommands () {
        // TODO: Add usage to addCommandConsumer for commands along with their help sections
        addCommandConsumer("clear", this::clearCommand);
        addCommandConsumer("exit", this::exitCommand);
        addCommandConsumer("help", this::helpCommand);
        addCommandConsumer("status", this::statusCommand);
        addCommandConsumer("ssh", this::sshCommand);
    }
    
    private void clearCommand (Command cmd) {
        console.clear();
    }
    
    private void exitCommand (Command cmd) {
        System.exit(0);
    }
    
    private void helpCommand (Command cmd) throws BadArgumentsException {
        String commandUsage = "help [local | remote]";
        CommandInterpreter.checkNumArgs(commandUsage, 1, cmd.argsLen());
        CommandInterpreter.expectedOneOf(commandUsage, "location", cmd.getArg(0), "local", "remote");
        
        // Send the command to remote if the location argument is "remote"
        if (cmd.getArg(0).equals("remote")) {
            sendCommandToRemote = true;
            return;
        }
        
        console.println("--Local command interpreter help--");
        console.println("[help goes here]");
        // TODO: Local command interpreter help automatically scales with addCommandConsumer
    }
    
    private void statusCommand (Command cmd) {
        console.println("");
        
        // Keep repeating until the user hits enter
        while (!console.hasInputReady()) {
            ConnectionStatus status = system.checkServerConnection();
            
            // Move up to the status line and clear previous contents
            console.moveUp(1);
            console.clearLine();
            
            // Display the new status
            String output = "";
            boolean isOutputError = status != ConnectionStatus.OK;
            
            output = status.name() + " - ";
            switch (status) {
                case NO_CONNECTION:
                    output += "There is no connection to remote. Check comms.";
                    break;
                case NO_SERVER:
                    output += "A connection to remote exists, but the server is unresponsive.";
                    break;
                case OK:
                    output += "Connection is OK.";
                    break;
                default:
                    output += "Unknown status code.";
                    break;
            }
            
            if (!isOutputError) console.println(output);
            else console.printlnErr(output);
            
            // Try to wait some amount of time before retrying
            try { Thread.sleep(200); }
            catch (InterruptedException e) { }
        }
        
        // Clear any user input already waiting to be processed
        console.clearWaitingInputLines();
    }
    
    private void sshCommand (Command cmd) throws BadArgumentsException {
        // Arguments exceptions
        String commandUsage = "ssh [user]";
        CommandInterpreter.checkNumArgs(commandUsage, 1, cmd.argsLen());
        
        String user = cmd.getArg(0).toLowerCase();
        CommandInterpreter.expectedOneOf(commandUsage, "user", user, "lvuser", "admin");
        
        // Get host for ssh and generate command
        String host = DriverStationSocketHandler.getRoborioHost(system.getTeamNum());
        
        String[] puttyCommand = new String[] {
            "putty",
            "-ssh",
            user+"@"+host,
        };
        
        // Attempt to run the command to start PuTTY
        ProcessBuilder processBuilder = new ProcessBuilder(puttyCommand);
        try {
            processBuilder.start();
        } catch (IOException e) {
            console.printlnErr("Failed to launch PuTTY from the command line.");
            console.printlnErr("Install PuTTY and ensure it is in your PATH environment variable.");
        }
    }
    
    @Override
    public boolean processLine (String line) throws Command.ParseException, BadArgumentsException {
        sendCommandToRemote = false;
        boolean result = super.processLine(line);
        return result && !sendCommandToRemote;
    }
    
}
