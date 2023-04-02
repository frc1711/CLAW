package claw.rct.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import claw.rct.commands.RCTCommand;
import claw.actions.compositions.Context.TerminatedContextException;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.commands.CommandProcessor.CommandFunction;
import claw.rct.commands.CommandProcessor.HelpMessage;
import claw.rct.local.LocalSystem.ConnectionStatus;
import claw.rct.network.low.ConsoleManager;
import claw.rct.network.messages.LogDataMessage.LogData;

/**
 * A wrapper around the {@link CommandLineInterpreter}, prepared to process local (driverstation) commands. When a command
 * is not recognized locally, it should be sent to remote (roboRIO) to try processing it.
 */
public class LocalCommandInterpreter {
    
    private final LogDataStorage logDataStorage;
    private final LocalSystem system;
    
    /**
     * Because commands are sent to remote when the local command interpreter indicates that it does not recognize a command,
     * this sendCommandToRemote boolean can be set in a command consumer method in order to indicate to the processLine method
     * that it should indicate it does not recognize the command and thus must send the command to remote.
     */
    private final CommandLineInterpreter commandInterpreter = new CommandLineInterpreter();
    
    private List<LogData> newLogData = new ArrayList<LogData>();
    private final Object newLogDataLock = new Object();
    
    /**
     * Construct a new {@link LocalCommandInterpreter} with all the resources it requires in order to execute
     * local commands.
     */
    public LocalCommandInterpreter (LocalSystem system, LogDataStorage logDataStorage) {
        this.logDataStorage = logDataStorage;
        logDataStorage.addOnReceiveDataListener(this::receiveLogDataListener);
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
        
        addCommand("help", "help | help [command]",
            "Displays a help message explaining how to use all the commands, or one particular provided command.",
            this::helpCommand);
        
        addCommand("comms", "comms",
            "Displays the current status of the connection to the remote (the roboRIO), automatically updating over time. Press enter to stop.",
            this::commsCommand);
        
        addCommand("ssh", "ssh [user]",
            "Launches an Secure Socket Shell for the roboRIO, using either the user 'lvuser' or 'admin'.",
            this::sshCommand);
        
        addCommand("log", "log",
            "Print data with CLAWLoggers to the terminal when it is received from the robot.",
            this::logCommand);
        
        addCommand("config", "config [team number] [remote port]",
            "Configure the connection to the RCT server, setting the team number and server port.",
            this::configCommand);
    }
    
    /**
     * Processes a line as a command. If the command is not recognized by the interpreter, this will return {@code true},
     * and the command should be sent to remote to be processed.
     * @param console                   The {@link ConsoleManager} to put output to and take input from.
     * @param line                      The line to process as command-line input.
     * @return                          Whether or not to send the command to remote to be processed.
     * @throws RCTCommand.ParseException
     * @throws BadCallException
     */
    public boolean processLine (ConsoleManager console, String line) throws RCTCommand.ParseException, BadCallException, TerminatedContextException {
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
    
    private void clearCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        reader.allowNone();
        console.clear();
    }
    
    private void exitCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        System.exit(0);
    }
    
    private void configCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        int teamNum = reader.readArgInt("team number");
        int port = reader.readArgInt("RCT server port");
        reader.noMoreArgs();
        reader.allowNoOptions();
        reader.allowNoFlags();
        
        system.setServerPort(port);
        system.setTeamNum(teamNum);
        
        try {
            system.establishNewConnection();
            console.printlnSys("Successfully connected to the RCT server at " + system.getRoborioHost().orElse("[connection failed]"));
        } catch (IOException e) {
            console.printlnErr("Failed to connect to the RCT server.");
        }
        
    }
    
    private void helpCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        reader.allowNoOptions();
        reader.allowNoFlags();
        
        // Combine local help messages and remote into one list
        List<HelpMessage> helpMessages = new ArrayList<HelpMessage>(commandInterpreter.getHelpMessages());
        List<HelpMessage> remoteMessages = Arrays.asList(system.getRemoteHelpMessages());
        helpMessages.addAll(remoteMessages);
        
        // Sort alphabetically by command
        helpMessages.sort((a, b) -> a.command().compareTo(b.command()));
        
        // Get the command which should be displayed in particular (if one was provided)
        Optional<String> command = Optional.empty();
        if (reader.hasNextArg()) {
            // Create a set containing all command name options
            HashSet<String> commandNames = new HashSet<>();
            helpMessages.forEach(message -> commandNames.add(message.command()));
            
            // Get the command name
            command = Optional.of(reader.readArgOneOf(
                "command name",
                "Expected the name of an existing command.",
                commandNames
            ));
        }
        
        reader.noMoreArgs();
        
        // Print each help message, or the particular message
        console.println("");
        if (command.isPresent()) {
            HelpMessage helpMessage = null;
            for (HelpMessage msg : helpMessages) {
                if (msg.command().equals(command.get())) {
                    helpMessage = msg;
                    break;
                }
            }
            
            console.printlnSys(helpMessage.usage());
            console.println(ConsoleManager.formatMessage(helpMessage.helpDescription(), 2)+"\n");
        } else {
            for (HelpMessage helpMessage : helpMessages) {
                console.printlnSys(helpMessage.usage());
                console.println(ConsoleManager.formatMessage(helpMessage.helpDescription(), 2)+"\n");
            }
        }
    }
    
    private void commsCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        reader.allowNone();
        
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
    
    private void sshCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        reader.allowNoOptions();
        reader.allowNoFlags();
        
        // Get the user
        String user = reader.readArgOneOf("user", "SSH user must be either 'lvuser' or 'admin'.", "lvuser", "admin");
        reader.noMoreArgs();
        
        // Get host for ssh and generate command
        Optional<String> host = system.getRoborioHost();
        if (host.isEmpty()) {
            console.printlnErr("There is no connection to the roboRIO.");
            return;
        }
        
        String[] puttyCommand = new String[] {
            "putty",
            "-ssh",
            user+"@"+host.get(),
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
    
    private void logCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        // Repeat the logging loop until the user pressed a key
        while (!console.hasInputReady()) {
            
            // Synchronize with the newLogDataLock and only print data if there is new data
            synchronized (newLogDataLock) {
                if (newLogData.size() > 0) {
                    for (LogData data : newLogData)
                        printLogDataEvent(console, data);
                    
                    newLogData.clear();
                }
            }
        }
    }
    
    private void receiveLogDataListener (LogData[] data) {
        synchronized (newLogDataLock) {
            newLogData.addAll(Arrays.asList(data));
        }
    }
    
    private static void printLogDataEvent (ConsoleManager console, LogData data) throws TerminatedContextException {
        String logNamePrint = "["+data.logName+"] ";
        String messagePrint = data.data;
        
        if (data.isError) {
            console.printlnErr(logNamePrint);
            console.printlnErr(ConsoleManager.formatMessage(messagePrint, 2));
        } else {
            console.printlnSys(logNamePrint);
            console.println(ConsoleManager.formatMessage(messagePrint, 2));
        }
    }
    
}
