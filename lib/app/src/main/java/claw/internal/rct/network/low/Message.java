package claw.internal.rct.network.low;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * A message object which can be sent in serialized form to transfer data to or from the roboRIO.
 */
public abstract class Message implements Serializable {
    
    public static final long serialVersionUID = 4L;
    
    /**
     * Gets the serialized form of this message.
     * @return The {@code byte[]} serialized data.
     * 
     * @see {@link Message#readMessage(InputStream)}
     */
    public byte[] getData () {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(bytesOut);
            objOut.writeObject(this);
        } catch (IOException e) {
            // This IOException should never be thrown, as objOut.writeObject should only ever throw an exception
            // if there is a problem with the output stream (a simple ByteArrayOutputStream) or if the object
            // is not capable of being serialized
            throw new RuntimeException("Unexpected exception while serializing a message:\n" + e);
        }
        
        return bytesOut.toByteArray();
    }
    
    /**
     * Reads a {@link Message} object from an input stream.
     * @param inputStream   The {@code InputStream} to read the serialized message from.
     * @return              The {@code Message} object.
     * @throws IOException  If the input stream threw an i/o exception or if there was an issue deserializing
     * a {@code Message} object from the input stream.
     * 
     * @see {@link Message#getData()}
     */
    public static Message readMessage (InputStream inputStream) throws IOException {
        try {
            ObjectInputStream objIn = new ObjectInputStream(inputStream);
            return (Message)objIn.readObject();
        } catch (ClassCastException e) {
            throw new IOException("Expected but did not receive a Message object");
        } catch (ClassNotFoundException e) {
            throw new IOException("Unidentifiable Message type received");
        }
    }
    
}
