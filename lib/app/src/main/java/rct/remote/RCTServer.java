package rct.remote;

import java.io.IOException;

import rct.commands.Command.ParseException;
import rct.commands.CommandInterpreter.BadArgumentsException;
import rct.network.low.InstructionMessage;
import rct.network.low.ResponseMessage;
import rct.network.low.RobotSocketHandler;
import rct.network.messages.ConnectionCheckMessage;
import rct.network.messages.ConnectionResponseMessage;
import rct.network.messages.commands.CommandInputMessage;
import rct.network.messages.commands.ProcessKeepaliveLocal;
import rct.network.messages.commands.StartCommandMessage;
import rct.remote.CommandProcessHandler.TerminatedProcessException;

public class RCTServer {
    
    private static final long
        COMMAND_KEEPALIVE_DURATION_MILLIS = 1000,
        COMMAND_KEEPALIVE_SEND_INTERVAL_MILLIS = 200;
    
    private final RobotSocketHandler serverSocket;
    private final RemoteCommandInterpreter interpreter = new RemoteCommandInterpreter();
    private boolean successfullyStarted = false;
    
    private CommandProcessHandler commandProcessHandler;
    
    public RCTServer (int port) throws IOException {
        // Try to create a new server socket
        serverSocket = new RobotSocketHandler(port, this::receiveMessage, this::handleReceiverException);
    }
    
    public void start () throws IOException {
        // Do nothing if the server has already successfully started
        if (successfullyStarted) return;
        
        // Try to get a new connection
        serverSocket.getNewConnection();
        
        // If an exception has not yet been thrown, the server has started successfully
        successfullyStarted = true;
    }
    
    private void receiveMessage (InstructionMessage msg) {
        try {
            Class<?> msgClass = msg.getClass();
            
            if (msgClass == ConnectionCheckMessage.class)
                receiveConnectionCheckMessage((ConnectionCheckMessage)msg);
            
            if (msgClass == StartCommandMessage.class)
                receiveStartCommandMessage((StartCommandMessage)msg);
            
            if (msgClass == CommandInputMessage.class)
                receiveCommandInputMessage((CommandInputMessage)msg);
            
            if (msgClass == ProcessKeepaliveLocal.class)
                receiveKeepaliveMessage((ProcessKeepaliveLocal)msg);
        } catch (IOException e) {
            handleNonFatalServerException(e);
        }
    }
    
    private void receiveConnectionCheckMessage (ConnectionCheckMessage msg) throws IOException {
        serverSocket.sendResponseMessage(new ConnectionResponseMessage());
    }
    
    private void receiveStartCommandMessage (StartCommandMessage msg) {
        // Terminate the previous command process handler if one existed
        if (commandProcessHandler != null)
            commandProcessHandler.terminate(false);
        
        // Create a new CommandProcessHandler for the new command
        commandProcessHandler = new CommandProcessHandler(
            responseMessage -> this.sendResponseMessageForProcess(responseMessage),
            msg.commandProcessId,
            COMMAND_KEEPALIVE_DURATION_MILLIS,
            COMMAND_KEEPALIVE_SEND_INTERVAL_MILLIS);
        
        // Run the command process in a new thread (so that the socket receiver thread we're currently on doesn't block)
        new Thread(() -> {
            try {
                // Attempt to run the process via the command interpreter
                if (!interpreter.processLine(commandProcessHandler, msg.command)) {
                    commandProcessHandler.printlnErr("Command not recognized.");
                }
            } catch (ParseException e) {
                commandProcessHandler.printlnErr("Malformatted command: " + e.getMessage());
            } catch (BadArgumentsException e) {
                commandProcessHandler.printlnErr(e.getMessage());
            } catch (TerminatedProcessException e) { } // If the process was terminated (a runtime exception), exit silently
            
            // When the command process is finished, terminate the process and flush all output to local
            commandProcessHandler.terminate(true);
        }).start();
        
    }
    
    private void sendResponseMessageForProcess (ResponseMessage msg) {
        try {
            serverSocket.sendResponseMessage(msg);
        } catch (IOException e) {
            commandProcessHandler.terminate(false);
        }
    }
    
    private void receiveKeepaliveMessage (ProcessKeepaliveLocal msg) {
        if (commandProcessHandler != null)
            commandProcessHandler.receiveKeepaliveMessage(msg);
    }
    
    private void receiveCommandInputMessage (CommandInputMessage msg) throws IOException {
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
            System.err.println("Nonfatal RCT server exception: " + e.getMessage());
            serverSocket.getNewConnection();
        } catch (IOException fatalEx) {
            handleFatalServerException(fatalEx);
        }
    }
    
    private void handleFatalServerException (IOException e) {
        System.err.println("Fatal RCT server exception: " + e.getMessage());
    }
    
}
