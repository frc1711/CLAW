package rct.local;

import java.io.IOException;
import java.util.function.Consumer;

import rct.commands.Command;
import rct.commands.CommandInterpreter.BadArgumentsException;
import rct.network.low.ConsoleManager;
import rct.network.low.DriverStationSocketHandler;
import rct.network.low.InstructionMessage;
import rct.network.low.ResponseMessage;
import rct.network.low.Waiter;
import rct.network.low.Waiter.NoValueReceivedException;
import rct.network.messages.ConnectionCheckMessage;
import rct.network.messages.ConnectionResponseMessage;
import rct.network.messages.StreamDataMessage;
import rct.network.messages.commands.CommandOutputMessage;
import rct.network.messages.commands.ProcessKeepaliveRemote;

/**
 * A interface between the robot control terminal and the socket connection to the robot.
 * Some functionality includes processing commands and controlling the messages sent to remote,
 * testing the status of the connection, and establishing new connections.
 */
public class LocalSystem {
    
    // Console manager
    private final ConsoleManager console;
    
    // Command interpreter
    private final LocalCommandInterpreter interpreter;
    private RemoteProcessHandler remoteProcessHandler = null;
    private int nextRemoteProcessId = 0;
    
    private final long
        responseTimeoutMillis,
        sendKeepaliveMessageIntervalMillis;
    
    // Stream data
    private final StreamDataStorage streamDataStorage;
    
    // Socket handling
    private DriverStationSocketHandler socket = null;
    private final Consumer<IOException> handleSocketReceiverException;
    private final int teamNum;
    
    // Server connection testing
    private final Waiter<ConnectionResponseMessage> connectionResponseWaiter = new Waiter<ConnectionResponseMessage>();
    
    /**
     * Create a new {@link LocalSystem} with a socket connection opened with the roboRIO.
     * @param teamNum                       The team number to use for the roboRIO
     * @param remotePort                    The remote port to connect to (if you don't know what to try, 5800 is a good default).
     * @param responseTimeout               The amount 
     * @param streamDataStorage
     * @param console
     * @param handleSocketReceiverException
     * @throws NoRunningServerException
     * @throws IOException
     */
    public LocalSystem (
            int teamNum,
            int remotePort,
            double responseTimeout,
            double sendKeepaliveMessageInterval,
            StreamDataStorage streamDataStorage,
            ConsoleManager console,
            Consumer<IOException> handleSocketReceiverException)
            throws NoRunningServerException, IOException {
        
        // Instantiating final fields
        this.teamNum = teamNum;
        this.streamDataStorage = streamDataStorage;
        this.console = console;
        this.handleSocketReceiverException = handleSocketReceiverException;
        interpreter = new LocalCommandInterpreter(this, streamDataStorage);
        responseTimeoutMillis = (long)(1000*responseTimeout);
        sendKeepaliveMessageIntervalMillis = (long)(1000*sendKeepaliveMessageInterval);
        
        // Establishing socket connection
        console.printlnSys("Connecting to "+DriverStationSocketHandler.getRoborioHost(teamNum)+":"+remotePort+"...");
        try {
            establishNewConnection(remotePort);
        } catch (IOException e) {
            console.printlnErr("Failed to connect to roboRIO.");
            throw e;
        }
        
        // Checking for a running server
        console.printlnSys("Socket connection successful. Checking for running RCT server...");
        if (checkServerConnection() != ConnectionStatus.OK) {
            console.printlnErr("No running Robot Control Terminal was detected. Try restarting the robot code.");
            closeSocket();
            throw new NoRunningServerException();
        }
        
        console.printlnSys("Successfully connected to roboRIO.");
    }
    
    /**
     * Try to recreate the {@link DriverStationSocketHandler}, connecting to {@link DriverStationSocketHandler#getRoborioHost(int)}
     * at the given remote port.
     * @param remotePort    The remote port to connect to.
     * @throws IOException  If an i/o error occurred while trying to open the socket.
     */
    public void establishNewConnection (int remotePort) throws IOException {
        // Close the current socket (if one exists)
        try {
            if (socket != null) socket.close();
        } catch (IOException e) { }
        
        // Create a new socket
        socket = new DriverStationSocketHandler(teamNum, remotePort, this::receiveMessage, handleSocketReceiverException);
    }
    
    /**
     * Gets the team number passed in through the constructor.
     */
    public int getTeamNum () {
        return teamNum;
    }
    
    /**
     * Checks the connection status.
     * @return The {@link ConnectionStatus} describing the current connection to the server.
     */
    public ConnectionStatus checkServerConnection () {
        // Attempt to send a connection check message to remote
        try {
            socket.sendInstructionMessage(new ConnectionCheckMessage());
        } catch (IOException e) {
            return ConnectionStatus.NO_CONNECTION;
        }
        
        // Try to wait for a response back (connectionResponseWaiter will be notified by the receiver thread)
        try {
            connectionResponseWaiter.waitForValue(responseTimeoutMillis);
            
            // Return an OK connection status because a connection response message was received
            return ConnectionStatus.OK;
        } catch (NoValueReceivedException e) {
            
            // Return a NO_SERVER connection status because no connection response message was received
            return ConnectionStatus.NO_SERVER;
        }
    }
    
    /**
     * Executes a command provided by an input string.
     * <ol>
     * <li>A local command interpreter will attempt to process the command. If it succeeds, then
     * the command has been successfully processed and the method returns here.</li>
     * <li>If the command is not recognized by the local interpreter, it is sent to remote
     * to be processed. This waiting for a response is blocking. If no response is received
     * within the timeout, a {@link NoResponseException} will be thrown.</li>
     * </ol>
     * @param line                      The input string to be processed into a command.
     * @throws Command.ParseException   If the provided command string if malformed
     * @throws NoResponseException      If no response was received from a command sent to remote
     * @throws IOException              If the command failed to send to remote
     */
    public void processCommand (String line) throws Command.ParseException, NoResponseException, IOException, BadArgumentsException {
        // Attempt to process the command locally
        if (interpreter.processLine(console, line)) return;
        
        // If a remote process handler is running, terminate it
        if (remoteProcessHandler != null)
            remoteProcessHandler.terminate();
        
        // Create a new remoteProcessHandler
        remoteProcessHandler = new RemoteProcessHandler(
            console,
            this::remoteProcessHandlerSendInstructionMessage,
            responseTimeoutMillis,
            sendKeepaliveMessageIntervalMillis,
            nextRemoteProcessId);
        
        // Increment the remote process ID so that the next process uses a new ID
        nextRemoteProcessId ++;
        remoteProcessHandler.execute(line);
    }
    
    /**
     * Represents a connection status with remote.
     */
    public enum ConnectionStatus {
        /**
         * The socket connection failed.
         */
        NO_CONNECTION,
        
        /**
         * There is a socket connection to remote, but the RCT server is not responding or there is no server running.
         */
        NO_SERVER,
        
        /**
         * The connection is OK.
         */
        OK,
    }
    
    /**
     * Receives a generalized {@link ResponseMessage}, and delegates responsibility
     * based on the message's subclass. This method will run on a receiver thread.
     */
    private void receiveMessage (ResponseMessage msg) {
        Class<?> msgClass = msg.getClass();
        
        if (msgClass == ConnectionResponseMessage.class)
            receiveConnectionResponseMessage((ConnectionResponseMessage)msg);
        
        if (msgClass == ProcessKeepaliveRemote.class)
            receiveProcessKeepaliveMessage((ProcessKeepaliveRemote)msg);
        
        if (msgClass == CommandOutputMessage.class)
            receiveCommandOutputMessage((CommandOutputMessage)msg);
        
        if (msgClass == StreamDataMessage.class)
            receiveStreamDataMessage((StreamDataMessage)msg);
    }
    
    /**
     * Receives a connection response message, notifying the connectionResponseWaiter
     * so that the response can be processed.
     * This method will run on a receiver thread, and is delegated a message from
     * {@link LocalSystem#receiveMessage(ResponseMessage)}.
     */
    private void receiveConnectionResponseMessage (ConnectionResponseMessage msg) {
        connectionResponseWaiter.receive(msg);
    }
    
    private void receiveProcessKeepaliveMessage (ProcessKeepaliveRemote msg) {
        if (remoteProcessHandler != null)
            remoteProcessHandler.receiveKeepalive(msg);
    }
    
    /**
     * Receives a stream data message and sends it to the stream data storage to be
     * processed. This method will run on a receiver thread, and is delegated a message from
     * {@link LocalSystem#receiveMessage(ResponseMessage)}.
     */
    private void receiveStreamDataMessage (StreamDataMessage msg) {
        streamDataStorage.acceptDataMessage(msg);
    }
    
    /**
     * Closes the socket. See {@link DriverStationSocketHandler#close()}.
     * @throws IOException If the socket threw an i/o exception while closing.
     */
    public void closeSocket () throws IOException {
        socket.close();
    }
    
    // COMMAND INTERPRETATION
    
    private void receiveCommandOutputMessage (CommandOutputMessage msg) {
        if (remoteProcessHandler != null)
            remoteProcessHandler.receiveCommandOutputMessage(msg);
    }
    
    private void remoteProcessHandlerSendInstructionMessage (InstructionMessage message) {
        try {
            socket.sendInstructionMessage(message);
        } catch (IOException e) {
            remoteProcessHandler.terminate();
        }
    }
    
    /**
     * An exception thrown if no response was received from remote after a command input message was sent.
     */
    public static class NoResponseException extends IOException {
        public NoResponseException () {
            super("No response from the roboRIO was received for the last executed command.");
        }
    }
    
    /**
     * An exception thrown if no response was received from remote after a connection check message was sent.
     */
    public class NoRunningServerException extends IOException {
        public NoRunningServerException () {
            super(
                "No instance of the Robot Control Terminal server is running, " +
                "or it has not responded within the " + responseTimeoutMillis +
                "ms timeout"
            );
        }
    }
    
}
