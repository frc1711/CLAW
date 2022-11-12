package rct.low;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DriverStationSocket {
    
    private final SocketChannel socket;
    
    public DriverStationSocket (int teamNum, int port) throws UnknownHostException, SocketException, IOException {
        System.out.println("CREATING A NEW SOCKET");
        socket = SocketChannel.open(new InetSocketAddress("roboRIO-"+teamNum+"-frc.local", port));
    }
    
    public void sendInstructionMessage (InstructionMessage message) throws IOException {
        socket.write(ByteBuffer.wrap(message.getData()));
    }
    
    public ResponseMessage readResponseMessage () throws IOException {
        ObjectInputStream objIn = new ObjectInputStream(socket.socket().getInputStream());
        try {
            ResponseMessage message = (ResponseMessage)objIn.readObject();
            return message;
        } catch (ClassCastException e) {
            throw new IOException("Read in an object that was not a ResponseMessage when a ResponseMessage was expected");
        } catch (ClassNotFoundException e) {
            throw new IOException("Read in an unidentifiable class but a ResponseMessage was expected");
        } catch (IOException e) {
            throw new IOException("IOException encountered while reading a ResponseMessage:\n" + e);
        }
    }
    
    public void close () throws IOException {
        socket.close();
    }
    
}
