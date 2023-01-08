package claw.rct.remote;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import claw.Registry;
import claw.logs.LogHandler;
import claw.logs.RCTLog;
import claw.rct.commands.CommandLineInterpreter.CommandLineException;
import claw.rct.network.low.InstructionMessage;
import claw.rct.network.low.ResponseMessage;
import claw.rct.network.low.RobotSocketHandler;
import claw.rct.network.messages.ConnectionCheckMessage;
import claw.rct.network.messages.ConnectionResponseMessage;
import claw.rct.network.messages.StreamDataMessage;
import claw.rct.network.messages.commands.CommandInputMessage;
import claw.rct.network.messages.commands.ProcessKeepaliveLocal;
import claw.rct.network.messages.commands.StartCommandMessage;
import claw.rct.remote.CommandProcessHandler.TerminatedProcessException;
import claw.subsystems.SubsystemCLAW;

public class RCTServer {
    
    private static final RCTLog LOG = LogHandler.getSysLog("Server");
    
    private static final long
        COMMAND_KEEPALIVE_DURATION_MILLIS = 1000,
        COMMAND_KEEPALIVE_SEND_INTERVAL_MILLIS = 200;
    
    private final RobotSocketHandler serverSocket;
    private final RemoteCommandInterpreter interpreter;
    private boolean successfullyStarted = false;
    
    private CommandProcessHandler commandProcessHandler;
    
    public RCTServer (int port, Registry<SubsystemCLAW> subsystemRegistry) throws IOException {
        // Try to create a new server socket
        serverSocket = new RobotSocketHandler(port, this::receiveMessage, this::handleReceiverException);
        interpreter = new RemoteCommandInterpreter(subsystemRegistry);
    }
    
    public void start () throws IOException {
        // Do nothing if the server has already successfully started
        if (successfullyStarted) return;
        
        // Try to get a new connection
        serverSocket.getNewConnection();
        
        // If an exception has not yet been thrown, the server has started successfully
        successfullyStarted = true;
    }
    
    public void sendStreamDataMessage (StreamDataMessage message) throws IOException {
        serverSocket.sendResponseMessage(message);
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
        Thread commandProcessorThread = new Thread(() -> {
            try {
                // Attempt to run the process via the command interpreter
                interpreter.processLine(commandProcessHandler, msg.command);
            } catch (CommandLineException e) {
                e.writeToConsole(commandProcessHandler);
            } catch (TerminatedProcessException e) { } // If the process was terminated (a runtime exception), exit silently
            
            // When the command process is finished, terminate the process and flush all output to local
            commandProcessHandler.terminate(true);
        });
        
        commandProcessorThread.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
            commandProcessHandler.terminate(false);
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
        });
        
        commandProcessorThread.start();
        
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
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String message = "Nonfatal RCT server exception:\n" + stringWriter.toString();
            
            System.err.println(message);
            LOG.err(message);
            
            serverSocket.getNewConnection();
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
