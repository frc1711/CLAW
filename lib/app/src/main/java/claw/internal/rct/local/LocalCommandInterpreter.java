package claw.internal.rct.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import claw.internal.rct.commands.Command;
import claw.internal.rct.commands.CommandLineInterpreter;
import claw.internal.rct.commands.CommandProcessor;
import claw.internal.rct.commands.CommandReader;
import claw.internal.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.internal.rct.commands.CommandProcessor.BadCallException;
import claw.internal.rct.commands.CommandProcessor.CommandFunction;
import claw.internal.rct.commands.CommandProcessor.HelpMessage;
import claw.internal.rct.local.LocalSystem.ConnectionStatus;
import claw.internal.rct.network.low.ConsoleManager;
import claw.internal.rct.network.low.DriverStationSocketHandler;
import claw.internal.rct.network.messages.LogDataMessage.LogData;

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
    
    private boolean hasNewLogData = false;
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
        
        addCommand("help", "help [location]",
            "Displays a help message for the given location, either local (driverstation) or remote (roboRIO).",
            this::helpCommand);
        
        addCommand("comms", "comms",
            "Displays the current status of the connection to the remote (the roboRIO), automatically updating over time. Press enter to stop.",
            this::commsCommand);
        
        addCommand("ssh", "ssh [user]",
            "Launches an Secure Socket Shell for the roboRIO, using either the user 'lvuser' or 'admin'.",
            this::sshCommand);
        
        addCommand("log", "log [--live]",
            "Log will, by default, print logged data to the terminal when it is received from the robot.\n" +
            "Live mode can be enabled using 'log --live' or 'log -l', where different logs are updated in their\n" +
            "own lines in the terminal rather than all being printed to new lines. This can be useful for tracking\n" +
            "several changing variables over time.",
            this::logCommand);
    }
    
    /**
     * Processes a line as a command. If the command is not recognized by the interpreter, this will return {@code true},
     * and the command should be sent to remote to be processed.
     * @param console                   The {@link ConsoleManager} to put output to and take input from.
     * @param line                      The line to process as command-line input.
     * @return                          Whether or not to send the command to remote to be processed.
     * @throws Command.ParseException
     * @throws BadCallException
     */
    public boolean processLine (ConsoleManager console, String line) throws Command.ParseException, BadCallException {
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
    
    
    
    private void clearCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        console.clear();
    }
    
    private void exitCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        System.exit(0);
    }
    
    private void helpCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        
        List<HelpMessage> helpMessages = commandInterpreter.getHelpMessages();
        
        console.printlnSys("\n==== Local Command Interpreter Help ====");
        console.println("All the following commands run on the local command interpreter, meaning they");
        console.println("are executed on the driverstation and not the roboRIO (with few exceptions).\n");
        for (HelpMessage helpMessage : helpMessages) {
            console.printlnSys(helpMessage.usage);
            console.println("  " + helpMessage.helpDescription + "\n");
        }
    }
    
    private void commsCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
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
    
    private void sshCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNoOptions();
        reader.allowNoFlags();
        
        // Get the user
        String user = reader.readArgOneOf("user", "SSH user must be either 'lvuser' or 'admin'.", "lvuser", "admin");
        reader.noMoreArgs();
        
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
    
    private void logCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowFlags('l');
        reader.allowOptions("live");
        
        boolean liveLogging = reader.getFlag('l') || reader.getOptionMarker("live");
        
        LiveDataLines lines = new LiveDataLines();
        
        while (!console.hasInputReady()) {
            synchronized (newLogDataLock) {
                if (hasNewLogData) {
                    hasNewLogData = false;
                    
                    if (liveLogging) {
                        lines.updateDisplay(console, newLogData);
                    } else {
                        for (LogData data : newLogData) {
                            printLogDataEvent(console, data);
                        }
                    }
                    
                    newLogData.clear();
                }
            }
        }
    }
    
    private void receiveLogDataListener (LogData[] data) {
        synchronized (newLogDataLock) {
            newLogData.addAll(Arrays.asList(data));
            hasNewLogData = true;
        }
    }
    
    private static void printLogDataEvent (ConsoleManager console, LogData data) {
        String logNamePrint = "["+data.logDomain+"] ";
        String messagePrint = data.data;
        
        if (data.isError) {
            console.printlnErr(logNamePrint + messagePrint);
        } else {
            console.printSys(logNamePrint);
            console.println(messagePrint);
        }
    }
    
    private static class LiveDataLines {
        
        private final List<LogData> dataLines = new ArrayList<>();
        
        public LiveDataLines () { }
        
        private void receiveLogData (List<LogData> dataSet) {
            for (LogData data : dataSet) {
                boolean hasFoundLine = false;
                
                for (int i = 0; i < dataLines.size(); i ++) {
                    if (data.logDomain.equals(dataLines.get(i).logDomain)) {
                        dataLines.set(i, data);
                        hasFoundLine = true;
                    }
                }
                
                if (!hasFoundLine)
                    dataLines.add(data);
            }
        }
        
        public void updateDisplay (ConsoleManager console, List<LogData> dataSet) {
            int numLines = dataLines.size();
            
            receiveLogData(dataSet);
            
            while (numLines > 0) {
                console.moveUp(1);
                console.clearLine();
                numLines --;
            }
            
            for (int i = 0; i < dataLines.size(); i ++) {
                LogData data = dataLines.get(i);
                
                String domainMsg = data.logDomain + ": ";
                String message = data.data.split("\n")[0]; // Prevent more than one line being printed
                
                boolean hasBeenCut = message.length() != data.data.length();
                
                if (message.length() > 60) {
                    message = message.substring(0, 60);
                    hasBeenCut = true;
                }
                
                if (hasBeenCut) message += "...";
                
                if (data.isError) {
                    console.printlnErr(domainMsg + message);
                } else {
                    console.printSys(domainMsg);
                    console.println(message);
                }
            }
        }
        
    }
    
}
