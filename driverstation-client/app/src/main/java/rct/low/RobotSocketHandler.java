package rct.low;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.function.Consumer;

public class RobotSocketHandler {
    
    private final ServerSocketChannel serverSocket;
    private final Consumer<InstructionMessage> instructionConsumer;
    private final ArrayList<SocketChannel> clientSockets = new ArrayList<SocketChannel>();
    
    public RobotSocketHandler (int port, Consumer<InstructionMessage> instructionConsumer) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(true);
        serverSocket.bind(new InetSocketAddress(port));
        this.instructionConsumer = instructionConsumer;
    }
    
    public void awaitConnections () throws IOException {
        while (true) {
            SocketChannel clientSocket = serverSocket.accept();
            handleClientSocket(clientSocket);
        }
    }
    
    private void handleClientSocket (SocketChannel socket) {
        clientSockets.add(socket);
        
        Thread thread = new Thread(() -> {
            try {
                while (socket.isOpen()) readInstructionMessage(new ObjectInputStream(socket.socket().getInputStream()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Read in an unidentifiable class but an InstructionMessage was expected");
            } catch (ClassCastException e) {
                throw new RuntimeException("Read in an object that was not an InstructionMessage when an InstructionMessage was expected");
            } catch (IOException e) {
                throw new RuntimeException("IOException encountered while reading an InstructionMessage:\n" + e);
            }
        });
        
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException (Thread thread, Throwable exception) {
                System.out.println("Uncaught exception in thread handling robot controls terminal driverstation client socket " +
                    socket.socket().getInetAddress().getHostAddress() + ": " + exception.getMessage());
            }
        });
        
        thread.start();
    }
    
    private void readInstructionMessage (ObjectInputStream objIn) throws ClassNotFoundException, ClassCastException, IOException {
        instructionConsumer.accept((InstructionMessage)objIn.readObject());
    }
    
    public void sendResponseMessage (ResponseMessage responseMessage) throws IOException {
        String exceptionsString = "";
        
        for (SocketChannel socket : clientSockets) {
            try {
                socket.write(ByteBuffer.wrap(responseMessage.getData()));
            } catch (IOException e) {
                exceptionsString += e.getMessage() + "\n";
            }
        }
        
        if (!exceptionsString.equals(""))
            throw new IOException("While sending a response message for the robot control terminal, " +
                "the following exception(s) occurred:\n" + exceptionsString);
    }
    
    public void close () throws IOException {
        serverSocket.close();
    }
    
}
