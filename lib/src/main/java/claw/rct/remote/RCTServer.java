package claw.rct.remote;

import java.io.IOException;
import java.util.ArrayList;

import claw.logs.CLAWLogger;
import claw.rct.base.commands.CommandLineInterpreter;
import claw.rct.base.commands.CommandLineInterpreter.CommandLineException;
import claw.rct.base.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.rct.base.commands.CommandProcessor.HelpMessage;
import claw.rct.base.console.ConsoleManager.TerminalKilledException;
import claw.rct.base.network.low.ResponseMessage;
import claw.rct.base.network.low.RobotSocketHandler;
import claw.rct.base.network.messages.CommandsListingMessage;
import claw.rct.base.network.messages.ConnectionCheckMessage;
import claw.rct.base.network.messages.ConnectionResponseMessage;
import claw.rct.base.network.messages.InstructionMessageHandler;
import claw.rct.base.network.messages.LogDataMessage;
import claw.rct.base.network.messages.commands.CommandInputMessage;
import claw.rct.base.network.messages.commands.ProcessKeepaliveLocal;
import claw.rct.base.network.messages.commands.StartCommandMessage;

public class RCTServer implements InstructionMessageHandler {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.server");
    
    private static final long
        COMMAND_KEEPALIVE_DURATION_MILLIS = 1000,
        COMMAND_KEEPALIVE_SEND_INTERVAL_MILLIS = 200;
    
    private final RobotSocketHandler serverSocket;
    private final RemoteCommandInterpreter interpreter;
    private final CommandLineInterpreter extensibleInterpreter;
    private boolean successfullyStarted = false;
    
    private CommandProcessHandler commandProcessHandler;
    
    public RCTServer (int port, CommandLineInterpreter extensibleInterpreter) throws IOException {
        // Try to create a new server socket
        serverSocket = new RobotSocketHandler(port, this::receiveMessage, this::handleReceiverException);
        interpreter = new RemoteCommandInterpreter();
        this.extensibleInterpreter = extensibleInterpreter;
    }
    
    private void waitForConnection () throws IOException {
        // Establish a new connection
        serverSocket.getNewConnection();
        
        System.out.println("DriverStation Robot Control Terminal connected.");
        
        // Try to send a commands listing message
        try {
            sendCommandsListingMessage();
        } catch (IOException e) {
            handleNonFatalServerException(e);
        }
    }
    
    public void start () throws IOException {
        // Do nothing if the server has already successfully started
        if (successfullyStarted) return;
        
        // Try to get a new connection
        waitForConnection();
        
        // If an exception has not yet been thrown, the server has started successfully
        successfullyStarted = true;
    }
    
    public void sendLogDataMessage (LogDataMessage message) throws IOException {
        serverSocket.sendResponseMessage(message);
    }
    
    private void sendCommandsListingMessage () throws IOException {
        // Get all help messages from both interpreters
        ArrayList<HelpMessage> helpMessages = new ArrayList<HelpMessage>();
        helpMessages.addAll(interpreter.getHelpMessages());
        helpMessages.addAll(extensibleInterpreter.getHelpMessages());
        
        // Turn the arraylist into an array and send the response
        serverSocket.sendResponseMessage(new CommandsListingMessage(helpMessages.toArray(new HelpMessage[0])));
    }
    
    @Override
    public void receiveConnectionCheckMessage (ConnectionCheckMessage msg) {
        try {
            serverSocket.sendResponseMessage(new ConnectionResponseMessage());
        } catch (IOException e) {
            handleNonFatalServerException(e);
        }
    }
    
    @Override
    public void receiveStartCommandMessage (StartCommandMessage msg) {
        // Terminate the previous command process handler if one existed
        if (commandProcessHandler != null)
            commandProcessHandler.terminate();
        
        // Create a new CommandProcessHandler for the new command
        commandProcessHandler = new CommandProcessHandler(
            responseMessage -> this.sendResponseMessageForProcess(responseMessage),
            msg.commandProcessId,
            COMMAND_KEEPALIVE_DURATION_MILLIS,
            COMMAND_KEEPALIVE_SEND_INTERVAL_MILLIS);
        
        // Run the command process in a new thread (so that the socket receiver thread we're currently on doesn't block)
        Thread commandProcessorThread = new Thread(() -> {
            
            try {
                try {
                    try {
                        
                        // Attempt to run the process via the command interpreter
                        interpreter.processLine(commandProcessHandler, msg.command);
                        
                    } catch (CommandNotRecognizedException e) {
                        extensibleInterpreter.processLine(commandProcessHandler, msg.command);
                    }
                } catch (CommandLineException e) {
                    e.writeToConsole(commandProcessHandler);
                }
            } catch (TerminalKilledException e) { }
            
            // When the command process is finished, terminate the process
            commandProcessHandler.terminate();
            
            
        });
        
        commandProcessorThread.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
            commandProcessHandler.terminate();
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
        });
        
        commandProcessorThread.start();
        
    }
    
    public void sendResponseMessageForProcess (ResponseMessage msg) {
        try {
            serverSocket.sendResponseMessage(msg);
        } catch (IOException e) {
            commandProcessHandler.terminate();
        }
    }
    
    @Override
    public void receiveKeepaliveMessage (ProcessKeepaliveLocal msg) {
        if (commandProcessHandler != null)
            commandProcessHandler.receiveKeepaliveMessage(msg);
    }
    
    @Override
    public void receiveCommandInputMessage (CommandInputMessage msg) {
        if (commandProcessHandler != null)
            commandProcessHandler.receiveCommandInputMessage(msg);
    }
    
    // Exception handling:
    
    private void handleReceiverException (IOException e) {
        handleNonFatalServerException(e);
    }
    
    private void handleNonFatalServerException (IOException e) {
        // Try to get a new connection to the driverstation
        try {
            System.out.println("DriverStation Robot Control Terminal disconnected.");
            
            waitForConnection();
        } catch (IOException fatalEx) {
            handleFatalServerException(fatalEx);
        }
    }
    
    private void handleFatalServerException (IOException e) {
        String message = "Fatal RCT server exception: " + e.getMessage();
        System.err.println(message);
        LOG.err(message);
    }
    
}
