package rct.network.low;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Consumer;

public class RobotSocketHandler {
    
    private final ServerSocket serverSocket;
    private final Consumer<InstructionMessage> instructionReader;
    private final Consumer<IOException> excHandler;
    
    private SocketHandler clientSocketHandler;
    
    public RobotSocketHandler (
            int port,
            Consumer<InstructionMessage> instructionReader,
            Consumer<IOException> excHandler) throws IOException {
        serverSocket = new ServerSocket(port);
        this.instructionReader = instructionReader;
        this.excHandler = excHandler;
    }
    
    public void start () throws IOException {
        clientSocketHandler = new SocketHandler(serverSocket.accept(), this::receiveMessage, this::handleReceiverIOException);
    }
    
    private void receiveMessage (Message message) {
        try {
            instructionReader.accept((InstructionMessage)message);
        } catch (ClassCastException e) {
            String messageClass = message.getClass().getName();
            handleReceiverIOException(new IOException("Expected an InstructionMessage but received a "+messageClass));
        }
    }
    
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
    
    public void sendResponseMessage (ResponseMessage responseMessage) throws IOException {
        clientSocketHandler.sendMessage(responseMessage);
    }
    
    public void close () throws IOException {
        serverSocket.close();
        if (!clientSocketHandler.isClosed())
            clientSocketHandler.close();
    }
    
}
