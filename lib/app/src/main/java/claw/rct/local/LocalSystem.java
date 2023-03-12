package claw.rct.local;

import java.io.IOException;
import java.util.Optional;

import claw.rct.commands.RCTCommand;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.commands.CommandProcessor.HelpMessage;
import claw.rct.network.low.ConsoleManager;
import claw.rct.network.low.DriverStationSocketHandler;
import claw.rct.network.low.InstructionMessage;
import claw.rct.network.low.ResponseMessage;
import claw.rct.network.low.concurrency.Waiter;
import claw.rct.network.low.concurrency.Waiter.NoValueReceivedException;
import claw.rct.network.messages.CommandsListingMessage;
import claw.rct.network.messages.ConnectionCheckMessage;
import claw.rct.network.messages.ConnectionResponseMessage;
import claw.rct.network.messages.LogDataMessage;
import claw.rct.network.messages.ResponseMessageHandler;
import claw.rct.network.messages.LogDataMessage.LogData;
import claw.rct.network.messages.commands.CommandOutputMessage;
import claw.rct.network.messages.commands.ProcessKeepaliveRemote;

/**
 * A interface between the robot control terminal and the socket connection to the robot.
 * Some functionality includes processing commands and controlling the messages sent to remote,
 * testing the status of the connection, and establishing new connections.
 */
public class LocalSystem implements ResponseMessageHandler {
    
    private final static long
        RESPONSE_TIMEOUT_MILLIS = 2000,
        SEND_KEEPALIVE_INTERVAL_MILLIS = 250,
        REESTABLISH_CONNECTION_INTERVAL_MILLIS = 100;
    
    // Console manager
    private final ConsoleManager console;
    
    // Command interpreter
    private final LocalCommandInterpreter interpreter;
    private RemoteProcessHandler remoteProcessHandler = null;
    private int nextRemoteProcessId = 0;
    
    // log data
    private final LogDataStorage logDataStorage;
    
    // Socket handling
    private final int teamNum, remotePort;
    private DriverStationSocketHandler socket = null;
    
    private final Thread requireNewConnectionThread = new Thread(this::requireNewConnectionThreadRunnable);
    private boolean requireNewConnection = false;
    
    private boolean useStaticRoboRIOAddress;
    
    private Optional<HelpMessage[]> remoteHelpMessages = Optional.empty();
    private final Object remoteHelpMessagesLock = new Object();
    
    // Server connection testing
    private final Waiter<ConnectionResponseMessage> connectionResponseWaiter = new Waiter<ConnectionResponseMessage>();
    private ConnectionStatus lastConnectionStatus = ConnectionStatus.NO_CONNECTION;
    private IOException lastConnectionException = null;
    
    /**
     * Create a new {@link LocalSystem} with a socket connection opened with the roboRIO.
     * @param teamNum                       The team number to use for the roboRIO
     * @param remotePort                    The remote port to connect to (if you don't know what to try, 5800 is a good default).
     * @param logDataStorage
     * @param console
     */
    public LocalSystem (
            int teamNum,
            boolean useStaticAddress,
            int remotePort,
            LogDataStorage logDataStorage,
            ConsoleManager console) {
        
        // Instantiating final fields
        this.teamNum = teamNum;
        this.useStaticRoboRIOAddress = useStaticAddress;
        this.remotePort = remotePort;
        this.logDataStorage = logDataStorage;
        this.console = console;
        interpreter = new LocalCommandInterpreter(this, logDataStorage);
        
        // Attempt to establish socket connection
        try {
            establishNewConnection();
        } catch (IOException e) {
            console.println(e.getMessage());
        }
        
        // Start the requireNewConnectionThread, which will attempt to establish a new connection
        // whenever it is interrupted
        requireNewConnectionThread.start();
        
    }
    
    /**
     * Try to recreate the {@link DriverStationSocketHandler}, connecting to {@link DriverStationSocketHandler#getRoborioHost(boolean, int teamNum)}.
     * @throws IOException  If an i/o error occurred while trying to open the socket.
     */
    public synchronized void establishNewConnection () throws IOException {
        // Close the current socket (if one exists)
        try {
            DriverStationSocketHandler s = throwIfNullSocket();
            if (s != null) s.close();
        } catch (IOException e) { }
        
        // Create a new socket
        try {
            socket = new DriverStationSocketHandler(
                teamNum,
                useStaticRoboRIOAddress,
                remotePort,
                this::receiveMessage,
                this::handleSocketReceiverException
            );
            lastConnectionException = null;
            
            updateConnectionStatus(ConnectionStatus.OK);
        } catch (IOException exception) {
            // If there's an IOException (one that's different from the previous exception),
            // then log it and throw it again so whatever is calling establishNewConnection can do its own handling
            if (lastConnectionException == null || !lastConnectionException.getClass().equals(exception.getClass())) {
                logDataStorage.acceptDataMessage(new LogDataMessage(new LogData[]{
                    new LogData("$connection.exception", exception.toString(), true)
                }));
            }
            
            lastConnectionException = exception;
            throw exception;
            
        }
    }
    
    public void setUseStaticRoborioAddress (boolean useStaticAddress) {
        useStaticRoboRIOAddress = useStaticAddress;
    }
    
    public String getRoborioHost () {
        return DriverStationSocketHandler.getRoborioHost(useStaticRoboRIOAddress, teamNum) + ":" + remotePort;
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
            DriverStationSocketHandler s = throwIfNullSocket();
            s.sendInstructionMessage(new ConnectionCheckMessage());
        } catch (IOException e) {
            return updateConnectionStatus(ConnectionStatus.NO_CONNECTION);
        }
        
        // Try to wait for a response back (connectionResponseWaiter will be notified by the receiver thread)
        try {
            connectionResponseWaiter.waitForValue(RESPONSE_TIMEOUT_MILLIS);
            
            // Return an OK connection status because a connection response message was received
            return updateConnectionStatus(ConnectionStatus.OK);
        } catch (NoValueReceivedException e) {
            
            // Return a NO_SERVER connection status because no connection response message was received
            return updateConnectionStatus(ConnectionStatus.NO_SERVER);
        }
    }
    
    private ConnectionStatus updateConnectionStatus (ConnectionStatus status) {
        if (status != lastConnectionStatus) {
            logDataStorage.acceptDataMessage(new LogDataMessage(new LogData[]{
                new LogData(
                    "$connection",
                    "Connection status changed to " + status.name(),
                    status != ConnectionStatus.OK
                )
            }));
            lastConnectionStatus = status;
        }
        
        if (status == ConnectionStatus.NO_CONNECTION)
            requireNewConnection = true;
        
        return status;
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
     * @throws RCTCommand.ParseException   If the provided command string if malformed
     * @throws NoResponseException      If no response was received from a command sent to remote
     * @throws IOException              If the command failed to send to remote
     */
    public void processCommand (String line) throws RCTCommand.ParseException, IOException, BadCallException {
        // Attempt to process the command locally. If the command should be sent to remote,
        // then interpreter.processLine will return true
        if (!interpreter.processLine(console, line)) return;
        
        // If a remote process handler is running, terminate it
        if (remoteProcessHandler != null)
            remoteProcessHandler.terminate();
        
        // Create a new remoteProcessHandler
        remoteProcessHandler = new RemoteProcessHandler(
            console,
            this::remoteProcessHandlerSendInstructionMessage,
            RESPONSE_TIMEOUT_MILLIS,
            SEND_KEEPALIVE_INTERVAL_MILLIS,
            nextRemoteProcessId);
        
        // Increment the remote process ID so that the next process uses a new ID
        nextRemoteProcessId ++;
        remoteProcessHandler.execute(line);
    }
    
    /**
     * Receives a connection response message, notifying the connectionResponseWaiter
     * so that the response can be processed.
     * This method will run on a receiver thread, and is delegated a message from
     * {@link LocalSystem#receiveMessage(ResponseMessage)}.
     */
    @Override
    public void receiveConnectionResponseMessage (ConnectionResponseMessage msg) {
        connectionResponseWaiter.receive(msg);
    }
    
    @Override
    public void receiveProcessKeepaliveMessage (ProcessKeepaliveRemote msg) {
        if (remoteProcessHandler != null)
            remoteProcessHandler.receiveKeepalive(msg);
    }
    
    /**
     * Receives a log data message and sends it to the log
     * data storage to be processed. This method will run on a receiver thread, and is delegated a message from
     * {@link LocalSystem#receiveMessage(ResponseMessage)}.
     */
    @Override
    public void receiveLogDataMessage (LogDataMessage msg) {
        logDataStorage.acceptDataMessage(msg);
    }
    
    // COMMAND INTERPRETATION
    
    @Override
    public void receiveCommandOutputMessage (CommandOutputMessage msg) {
        if (remoteProcessHandler != null)
            remoteProcessHandler.receiveCommandOutputMessage(msg);
    }
    
    @Override
    public void receiveCommandsListingMessage (CommandsListingMessage msg) {
        synchronized (remoteHelpMessagesLock) {
            // Set the remote help messages field
            remoteHelpMessages = Optional.of(msg.helpMessages);
        }
    }
    
    public HelpMessage[] getRemoteHelpMessages () {
        synchronized (remoteHelpMessagesLock) {
            // Return a copy of the remote help messages
            return remoteHelpMessages.orElse(new HelpMessage[0]).clone();
        }
    }
    
    public void remoteProcessHandlerSendInstructionMessage (InstructionMessage message) {
        try {
            // Attempt to send the instruction message
            DriverStationSocketHandler s = throwIfNullSocket();
            s.sendInstructionMessage(message);
        } catch (IOException e) {
            // If the call threw an exception, terminate the remote process handler with an exception
            remoteProcessHandler.terminate(e);
            
            // Then handle the exception in the usual way
            handleSocketException(e);
        }
    }
    
    // EXCEPTION HANDLING
    
    private void handleSocketReceiverException (IOException e) {
        handleSocketException(e);
    }
    
    private void handleSocketException (IOException e) {
        // Update the connection status to NO_CONNECTION so the requireNewConnectionThread will try
        // to create a new connection
        updateConnectionStatus(ConnectionStatus.NO_CONNECTION);
    }
    
    private void requireNewConnectionThreadRunnable () {
        while (true) {
            // If a new connection needs to be established, try to do that
            checkServerConnection(); // This will update the requireNewConnection flag
            if (requireNewConnection) {
                try {
                    // Try to establish a new connection
                    establishNewConnection();
                    
                    // If an i/o exception has not been thrown then the connection was established successfully
                    requireNewConnection = false;
                } catch (IOException e) {
                    updateConnectionStatus(ConnectionStatus.NO_CONNECTION);
                }
            }
            
            // Sleep the thread until we need to check again
            try {
                Thread.sleep(REESTABLISH_CONNECTION_INTERVAL_MILLIS);
            } catch (InterruptedException e) { }
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
    
    private DriverStationSocketHandler throwIfNullSocket () throws IOException {
        DriverStationSocketHandler s = socket;
        if (s == null) throw new NoSocketException();
        return s;
    }
    
    /**
     * An exception thrown if an action is performed on the socket connection,
     * but the intial attempt to open the socket failed.
     */
    public static class NoSocketException extends IOException {
        public NoSocketException () {
            super("No socket has been initialized.");
        }
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
    
}
