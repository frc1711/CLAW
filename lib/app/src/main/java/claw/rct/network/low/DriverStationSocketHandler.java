package claw.rct.network.low;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * A wrapper around {@link SocketHandler}, specialized for handling the local (driverstation) side
 * of the connection.
 */
public class DriverStationSocketHandler {
    
    private final SocketHandler socketHandler;
    private final Consumer<ResponseMessage> responseReader;
    private final Consumer<IOException> excHandler;
    
    /**
     * Constructs a new {@link DriverStationSocketHandler}, starting a new socket connection to remote.
     * @param teamNum           The team number to use for connecting to the roboRIO (1711 if you're on a cool team).
     * @param port              The remote port to connect to.
     * @param responseReader    A {@link ResponseMessage} {@code Consumer<T>} which accepts messages as they are received from remote.
     * @param excHandler        A {@code Consumer<IOException>} which accepts i/o exceptions that occur when receiving messages from remote.
     * @throws IOException      If the socket throws an i/o exception.
     */
    public DriverStationSocketHandler (
            int teamNum,
            int port,
            Consumer<ResponseMessage> responseReader,
            Consumer<IOException> excHandler)
            throws IOException {
        
        Socket socket = null;
        try {
            socket = new Socket(getRoborioHost(teamNum), port);
            socketHandler = new SocketHandler(socket, this::receiveMessage, this::handleReceiverIOException);
        } catch (IOException e) {
            if (socket != null) socket.close();
            throw e;
        }
        this.responseReader = responseReader;
        this.excHandler = excHandler;
    }
    
    /**
     * Gets the roboRIO host URL.
     * @param teamNum   The team number associated with the roboRIO.
     * @return          The hostname for connecting to the rorboRIO
     */
    public static String getRoborioHost (int teamNum) {
        return "roboRIO-"+teamNum+"-frc.local";
    }
    
    /**
     * Called by the underlying {@link SocketHandler} when a message has been received.
     */
    private void receiveMessage (Message message) {
        ResponseMessage resMessage = null;
        try {
            resMessage = (ResponseMessage)message;
        } catch (ClassCastException e) {
            String messageClass = message.getClass().getName();
            handleReceiverIOException(new IOException("Expected a ResponseMessage but received a "+messageClass));
        }
        
        responseReader.accept(resMessage);
    }
    
    /**
     * Called by the underlying {@link SocketHandler} when an i/o exception has occurred while receiving a message.
     */
    private void handleReceiverIOException (IOException ioException) {
        // Try to close the socket, but do nothing if there is a problem in attempting to close it
        try {
            if (!socketHandler.isClosed())
                socketHandler.close();
        } catch (Exception e) { }
        
        // Handle the IO exception
        excHandler.accept(ioException);
    }
    
    /**
     * Sends a instruction message to remote.
     * @param message       The {@link InstructionMessage} to send.
     * @throws IOException  If the socket failed to send the message.
     */
    public void sendInstructionMessage (InstructionMessage message) throws IOException {
        socketHandler.sendMessage(message);
    }
    
    /**
     * Closes the socket. See {@link SocketHandler#close()}.
     * @throws IOException If the socket threw an i/o exception while closing.
     */
    public void close () throws IOException {
        socketHandler.close();
    }
    
}
