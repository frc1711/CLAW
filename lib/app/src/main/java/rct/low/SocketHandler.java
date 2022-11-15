package rct.low;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketHandler {
    
    private final Socket socket;
    private final Consumer<Message> messageReceiver;
    private final Consumer<IOException> excHandler;
    
    /**
     * Create a new {@link SocketHandler} which allows for both sending and receiving {@link Message} objects.
     * Message reception is handled in a separate thread.
     * @param socket            The {@link Socket} to send and receive data from
     * @param messageReceiver   A {@code Consumer<Message>} which accepts messages received from the socket
     * @param excHandler  A {@code Consumer<IOException>} which handles {@code IOException}s which occur
     * in the message reception thread.
     * @throws IOException
     */
    public SocketHandler (
            Socket socket,
            Consumer<Message> messageReceiver,
            Consumer<IOException> excHandler) throws IOException {
        this.socket = socket;
        this.messageReceiver = messageReceiver;
        this.excHandler = excHandler;
        beginReceivingMessages();
    }
    
    /**
     * Sends a message through the socket
     * @param sendMessage
     * @throws IOException
     */
    public void sendMessage (Message sendMessage) throws IOException {
        this.socket.getOutputStream().write(sendMessage.getData());
    }
    
    private void beginReceivingMessages () throws IOException {
        
        // Start a new thread to receive messages from
        Thread receiverThread = new Thread(() -> {
            try {
                
                // Continue to read messages until the socket is closed
                InputStream socketIn = socket.getInputStream();
                while (!socket.isClosed()) {
                    messageReceiver.accept(Message.readMessage(socketIn));
                }
                
            } catch (IOException e) {
                // If an IOException occurs, pass it to the message receiver exception handler
                excHandler.accept(e);
            }
        });
        
        receiverThread.start();
    }
    
    /**
     * Closes the socket
     * @throws IOException
     */
    public void close () throws IOException {
        socket.close();
    }
    
    /**
     * Returns whether or not the socket is closed
     */
    public boolean isClosed () {
        return socket.isClosed();
    }
    
}
