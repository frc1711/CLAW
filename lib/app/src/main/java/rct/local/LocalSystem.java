package rct.local;

import java.io.IOException;
import java.util.function.Consumer;

import rct.commands.Command;
import rct.network.low.DriverStationSocketHandler;
import rct.network.low.ResponseMessage;
import rct.network.messages.CommandInputMessage;
import rct.network.messages.CommandOutputMessage;
import rct.network.messages.StreamDataMessage;

public class LocalSystem {
    
    // Command interpreter
    private final LocalCommandInterpreter interpreter;
    private final Consumer<String> putOut, putErr;
    
    private CommandInputMessage msgAwaitingResponse = null;
    private int currentRemoteCommandId = 1;
    
    // Stream data
    private final StreamDataStorage streamDataStorage;
    
    // Socket handling
    private final DriverStationSocketHandler socket;
    
    public LocalSystem (
            int teamNum,
            int remotePort,
            Consumer<String> putOut,
            Consumer<String> putErr,
            StreamDataStorage streamDataStorage)
            throws IOException {
        
        socket = new DriverStationSocketHandler(teamNum, remotePort, this::receiveMessage, this::handleSocketException);
        interpreter = new LocalCommandInterpreter(streamDataStorage);
        this.streamDataStorage = streamDataStorage;
        
        this.putOut = putOut;
        this.putErr = putErr;
    }
    
    // SOCKET
    
    private void receiveMessage (ResponseMessage msg) {
        Class<?> msgClass = msg.getClass();
        
        if (msgClass == CommandOutputMessage.class)
            receiveCommandOutputMessage((CommandOutputMessage)msg);
        
        else if (msgClass == StreamDataMessage.class)
            receiveStreamDataMessage((StreamDataMessage)msg);
    }
    
    private void receiveStreamDataMessage (StreamDataMessage msg) {
        streamDataStorage.acceptDataMessage(msg);
    }
    
    private void handleSocketException (IOException e) {
        putErr.accept(e.toString());
    }
    
    /**
     * Closes the socket.
     * @throws IOException See {@link java.net.Socket#close()}
     */
    public void close () throws IOException {
        socket.close();
    }
    
    // COMMAND INTERPRETATION
    
    private void receiveCommandOutputMessage (CommandOutputMessage msg) {
        // Do nothing if there is no message awaiting a response or if the output is
        // for the wrong message
        if (!awaitingRemoteCommandProcess() || msgAwaitingResponse.id != msg.inputCmdMsgId)
            return;
        
        // An output for command we are awaiting a response for is ready,
        // msgAwaitingResponse can be reset to null to indicate we are no
        // longer waiting for a remote command process
        msgAwaitingResponse = null;
        if (msg.isError)
            putErr.accept(msg.commandOutput);
        else
            putOut.accept(msg.commandOutput);
    }
    
    /**
     * Sends a command line to remote to be processed and sets the {@code msgAwaitingResponse} to
     * indicate that we are now awaiting a remote command process.
     * @param command The command string to send to remote
     * @throws IOException If the socket threw an exception while attempting to send the command
     */
    private void sendCommandToRemote (String command) throws IOException {
        msgAwaitingResponse = new CommandInputMessage(currentRemoteCommandId, command);
        socket.sendInstructionMessage(msgAwaitingResponse);
        currentRemoteCommandId ++;
    }
    
    /**
     * Checks whether or not the local system is waiting for a remote command process
     * to finish and send back data.
     * @return {@code true} if the local system is awaiting a response, {@code false} otherwise
     */
    public boolean awaitingRemoteCommandProcess () {
        return msgAwaitingResponse != null;
    }
    
    /**
     * Executes a command provided by an input string.
     * <ol>
     * <li>A local command interpreter will attempt to process the command. If it succeeds, then
     * the command has been successfully processed and the method returns here.</li>
     * <li>If the command is not recognized by the local interpreter, it is sent to remote
     * to be processed. A flag is set to indicate that the local system is awaiting a
     * remote command process. See {@link #awaitingRemoteCommandProcess()}. This waiting for
     * a response is non-blocking and the method returns here.</li>
     * <li>However, the local system will still be expecting a command output from remote.
     * When this happens, {@code awaitingRemoteCommandProcess} will be reset and a
     * new command can be inputted into the local system. The output from the command
     * will also be sent back through the {@code putOut} and {@code putErr}
     * consumers in the {@link #LocalSystem(int, int, Consumer, Consumer, StreamDataStorage)}
     * constructor method.</li>
     * </ol>
     * @param line The input string to be processed into a command.
     * @return {@code false} if the command could not be processed because the local system
     * is already waiting on a remote command process, {@code true} otherwise.
     * @throws Command.ParseException If the provided command string if malformed
     * @throws IOException If the command failed to send to remote
     */
    public boolean processCommand (String line) throws Command.ParseException, IOException {
        if (awaitingRemoteCommandProcess()) return false;
        
        // Attempt to process the command locally
        if (interpreter.processLine(line)) return true;
        
        // If the command was not successfully processed locally, send it
        // to remote to attempt to process it
        sendCommandToRemote(line);
        
        return true;
    }
    
}
