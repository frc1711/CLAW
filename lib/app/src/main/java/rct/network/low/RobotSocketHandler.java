package rct.network.low;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Consumer;

/**
 * A socket handler specialized for handling the remote (roboRIO) side of the connection.
 */
public class RobotSocketHandler {
    
    private final ServerSocket serverSocket;
    private final Consumer<InstructionMessage> instructionReader;
    private final Consumer<IOException> excHandler;
    
    private SocketHandler clientSocketHandler;
    
    /**
     * Constructs a new {@link RobotSocketHandler}, starting a new server waiting for connections from the driver station.
     * @param port              The port to wait for connections on. Port 5800 is a good default choice.
     * @param instructionReader A {@code Consumer<T>} of {@link InstructionMessage}s which accepts instruction messages sent from
     * the driver station.
     * @param excHandler        A {@code Consumer<IOException>} which accepts i/o exceptions which occur on the message receiver thread.
     * @throws IOException      If the server socket threw an i/o exception while starting up.
     */
    public RobotSocketHandler (
            int port,
            Consumer<InstructionMessage> instructionReader,
            Consumer<IOException> excHandler) throws IOException {
        serverSocket = new ServerSocket(port);
        this.instructionReader = instructionReader;
        this.excHandler = excHandler;
    }
    
    /**
     * Waits for a new connection to the server (blocking). Once the new socket is opened, messages can be sent and received
     * through this {@link RobotSocketHandler}.
     * @throws IOException If there is an i/o exception while waiting for and starting a new socket.
     */
    public void start () throws IOException {
        clientSocketHandler = new SocketHandler(serverSocket.accept(), this::receiveMessage, this::handleReceiverIOException);
    }
    
    /**
     * Called by the underlying {@link SocketHandler} when a message is recevied.
     */
    private void receiveMessage (Message message) {
        try {
            instructionReader.accept((InstructionMessage)message);
        } catch (ClassCastException e) {
            String messageClass = message.getClass().getName();
            handleReceiverIOException(new IOException("Expected an InstructionMessage but received a "+messageClass));
        }
    }
    
    /**
     * Called by the underlying {@link SocketHandler} when an i/o exception occurs while a message is being received.
     */
    private void handleReceiverIOException (IOException ioException) {
        // Try to close the client socket, but do nothing if there is a problem in attempting to close it
        try {
            if (!clientSocketHandler.isClosed())
                clientSocketHandler.close();
        } catch (Exception e) { }
        
        // Handle the IO exception
        excHandler.accept(ioException);
        
        // Try to get a new client socket if the server socket is still open
        try {
            if (!serverSocket.isClosed())
                start();
        } catch (IOException e) {
            excHandler.accept(e);
        }
    }
    
    /**
     * Sends a {@link ResponseMessage} to local (the driverstation).
     * @param responseMessage   The {@code ResponseMessage} to send.
     * @throws IOException      If the socket threw an i/o exception while attempting to send the message.
     */
    public void sendResponseMessage (ResponseMessage responseMessage) throws IOException {
        clientSocketHandler.sendMessage(responseMessage);
    }
    
    /**
     * Closes both the server socket and any open sockets connecting to the driverstation.
     * @throws IOException If there was an i/o exception while closing any of the sockets.
     */
    public void close () throws IOException {
        serverSocket.close();
        if (!clientSocketHandler.isClosed())
            clientSocketHandler.close();
    }
    
}
