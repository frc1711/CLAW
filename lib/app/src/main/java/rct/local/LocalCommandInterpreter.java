package rct.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rct.commands.Command;
import rct.commands.CommandLineInterpreter;
import rct.commands.CommandProcessor;
import rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import rct.commands.CommandProcessor.BadArgumentsException;
import rct.commands.CommandProcessor.CommandFunction;
import rct.commands.CommandProcessor.HelpMessage;
import rct.local.LocalSystem.ConnectionStatus;
import rct.network.low.ConsoleManager;
import rct.network.low.DriverStationSocketHandler;

/**
 * A wrapper around the {@link CommandLineInterpreter}, prepared to process local (driverstation) commands. When a command
 * is not recognized locally, it should be sent to remote (roboRIO) to try processing it.
 */
public class LocalCommandInterpreter {
    
    private final StreamDataStorage streamDataStorage;
    private final LocalSystem system;
    
    /**
     * Because commands are sent to remote when the local command interpreter indicates that it does not recognize a command,
     * this sendCommandToRemote boolean can be set in a command consumer method in order to indicate to the processLine method
     * that it should indicate it does not recognize the command and thus must send the command to remote.
     */
    private final CommandLineInterpreter commandInterpreter = new CommandLineInterpreter();
    
    /**
     * Construct a new {@link LocalCommandInterpreter} with all the resources it requires in order to execute
     * local commands.
     */
    public LocalCommandInterpreter (LocalSystem system, StreamDataStorage streamDataStorage) {
        this.streamDataStorage = streamDataStorage;
        this.system = system;
        addCommands();
    }
    
    private void addCommands () {
        addCommand("clear", "clear",
            "Clears the console.",
            this::clearCommand);
        
        addCommand("exit", "exit",
            "Exits the robot control terminal immediately.",
            this::exitCommand);
        
        addCommand("help", "help [location]",
            "Displays a help message for the given location, either local (driverstation) or remote (roboRIO).",
            this::helpCommand);
        
        addCommand("comms", "comms",
            "Displays the current status of the connection to the remote (the roboRIO), automatically updating over time. Press enter to stop.",
            this::statusCommand);
        
        addCommand("ssh", "ssh [user]",
            "Launches an Secure Socket Shell for the roboRIO, using either the user 'lvuser' or 'admin'.",
            this::sshCommand);
    }
    
    /**
     * Processes a line as a command. If the command is not recognized by the interpreter, this will return {@code true},
     * and the command should be sent to remote to be processed.
     * @param console                   The {@link ConsoleManager} to put output to and take input from.
     * @param line                      The line to process as command-line input.
     * @return                          Whether or not to send the command to remote to be processed.
     * @throws Command.ParseException
     * @throws BadArgumentsException
     */
    public boolean processLine (ConsoleManager console, String line) throws Command.ParseException, BadArgumentsException {
        try {
            commandInterpreter.processLine(console, line);
        } catch (CommandNotRecognizedException e) {
            return true;
        }
        
        return false;
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        commandInterpreter.addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
    }
    
    
    
    
    
    
    
    
    // Command methods:
    
    
    
    private void clearCommand (ConsoleManager console, Command cmd) {
        console.clear();
    }
    
    private void exitCommand (ConsoleManager console, Command cmd) {
        System.exit(0);
    }
    
    private void helpCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.checkNumArgs(0, 1, cmd.argsLen());
        
        if (cmd.argsLen() == 0) {
            console.println("Use 'help local' for a list of local commands, and 'help remote' for a list of remote commands.");
            return;
        }
        
        List<HelpMessage> helpMessages = commandInterpreter.getHelpMessages();
        
        console.printlnSys("\n==== Local Command Interpreter Help ====");
        console.println("All the following commands run on the local command interpreter, meaning they");
        console.println("are executed on the driverstation and not the roboRIO (with few exceptions).\n");
        for (HelpMessage helpMessage : helpMessages) {
            console.printlnSys(helpMessage.usage);
            console.println("  " + helpMessage.helpDescription + "\n");
        }
    }
    
    private void statusCommand (ConsoleManager console, Command cmd) {
        console.println("");
        
        // Keep repeating until the user hits enter
        while (!console.hasInputReady()) {
            
            // Display a "trying connection" banner below where the connection will be displayed
            console.println("Trying connection...");
            
            // Get the new status
            ConnectionStatus status = system.checkServerConnection();
            
            // Get the new status to display
            String output = "";
            boolean isOutputError = status != ConnectionStatus.OK;
            
            switch (status) {
                case NO_CONNECTION:
                    output = "There are no communications with remote.";
                    break;
                case NO_SERVER:
                    output = "Communications with remote are OK, but the Robot Control Terminal server is unresponsive.";
                    break;
                case OK:
                    output = "Connection is OK.";
                    break;
            }
            
            // Remove the "trying connection" banner
            console.moveUp(1);
            console.clearLine();
            
            // Remove the old status banner
            console.moveUp(1);
            console.clearLine();
            
            // Print the new status banner
            console.printSys(status.name());
            console.print(" - ");
            if (!isOutputError) console.println(output);
            else console.printlnErr(output);
            
            // Try to wait some amount of time before retrying
            try { Thread.sleep(200); }
            catch (InterruptedException e) { }
        }
        
        // Clear any user input already waiting to be processed
        console.clearWaitingInputLines();
    }
    
    private void sshCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.checkNumArgs(1, cmd.argsLen());
        String user = cmd.getArg(0);
        CommandProcessor.expectedOneOf("user", user, "lvuser", "admin");
        
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
    
}
