package rct.remote;

import java.io.IOException;

import rct.commands.Command;
import rct.commands.CommandInterpreter.BadArgumentsException;
import rct.network.low.InstructionMessage;
import rct.network.low.RobotSocketHandler;
import rct.network.messages.ConnectionCheckMessage;
import rct.network.messages.ConnectionResponseMessage;
import rct.network.messages.commands.CommandInputMessage;
import rct.network.messages.commands.CommandOutputMessage;

public class RCTServer {
    
    private final RobotSocketHandler serverSocket;
    private final RemoteCommandInterpreter interpreter = new RemoteCommandInterpreter();
    private boolean successfullyStarted = false;
    
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
            
            if (msgClass == CommandInputMessage.class)
                receiveCommandInputMessage((CommandInputMessage)msg);
        } catch (IOException e) {
            handleNonFatalServerException(e);
        }
    }
    
    private void receiveConnectionCheckMessage (ConnectionCheckMessage msg) throws IOException {
        serverSocket.sendResponseMessage(new ConnectionResponseMessage());
    }
    
    private void receiveCommandInputMessage(CommandInputMessage msg) throws IOException {
        boolean isError = false;
        String output = "";
        
        try {
            boolean recognizedCommand = interpreter.processLine(msg.command);
            if (!recognizedCommand) {
                isError = true;
                output = "Unrecognized command.";
            } else {
                output = "Output test. Hard-coded to be the same for all recognized commands";
            }
        } catch (BadArgumentsException e) {
            isError = true;
            output = e.getMessage();
        } catch (Command.ParseException e) {
            isError = true;
            output = e.getMessage();
        }
        
        // TODO: Provide some extension of the ConsoleManager to the RemoteCommandInterpreter so that remote commands can also control the console
        serverSocket.sendResponseMessage(new CommandOutputMessage(isError, msg.id, output));
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
