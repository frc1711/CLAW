package rct.low;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.List;

/**
 * Represents an object which accepts a list of Byte arrays and stores a byte[][] buffer
 * representing a list of raw byte[] messages.<br>
 * <pre>
 * // Representation of RawMessagesBuffer in bytes:
 * // Part:        [SERIAL_VERSION]  [bufferId]  [messageCount]  [messageLength (1)] ...  [message bytes (1)] ...
 * // Description: serial version    id          # of messages   1st msg length ...       1st msg data ...
 * // Type:        int               int         int             int                      byte[]
 * </pre>
 */
public class RawMessagesBuffer {
    
    /**
     * A serial version which should update whenever the format of RawMessagesBuffer is updated
     */
    public static final int SERIAL_VERSION = 0xDA7A_0001;
    
    public final int bufferId;
    public final int messageCount;
    public final int[] messageLengths;
    public final byte[][] messagesData;
    
    /**
     * Creates a new RawMessagesBuffer with an integer id and a list of raw binary messages.
     */
    public RawMessagesBuffer (int id, List<Byte[]> byteArraysBuffer) {
        bufferId = id;
        messageCount = byteArraysBuffer.size();
        messageLengths = new int[messageCount];
        messagesData = new byte[messageCount][];
        
        // Loop through the list of Byte[] arrays
        for (int i = 0; i < messageCount; i ++) {
            
            // Set this message's length
            messageLengths[i] = byteArraysBuffer.get(i).length;
            
            // Create the byte[] message data
            Byte[] byteData = byteArraysBuffer.get(i);
            messagesData[i] = new byte[messageLengths[i]];
            
            // Fill the message data
            for (int j = 0; j < byteData.length; j ++)
                messagesData[i][j] = byteData[j];
        }
    }
    
    /**
     * Attempts to read a messages buffer from a data input stream, throwing an IOException if
     * unsuccessful.
     */
    public static RawMessagesBuffer readFromStream (DataInputStream dataIn) throws IOException {
        try {
            // Read and match serial versions
            final int serialVersion = dataIn.readInt();
            if (serialVersion != SERIAL_VERSION)
                throw new IOException("Malformed RawMessagesBuffer or outdated version");
            
            // Get header fields
            final int bufferId = dataIn.readInt();
            final int messageCount = dataIn.readInt();
            final int[] messageLengths = new int[messageCount];
            for (int i = 0; i < messageCount; i ++) messageLengths[i] = dataIn.readInt();
            
            // Get messages data
            final byte[][] messagesData = new byte[messageCount][];
            for (int i = 0; i < messageCount; i ++) {
                // Initialize buffer
                messagesData[i] = new byte[messageLengths[i]];
                if (messageLengths[i] == 0) continue;
                
                // Read into buffer, unless it is zero length
                final int bytesRead = dataIn.read(messagesData[i]);
                
                // If EOF was reached (before buffer reading was done), throw an EOF exception
                if (bytesRead == -1) throw new EOFException();
            }
            
            // Return the RawMessagesBuffer
            return new RawMessagesBuffer(bufferId, messageCount, messageLengths, messagesData);
            
        } catch (EOFException e) {
            throw new IOException("Malformed RawMessagesBuffer, read to end before expected");
        }
    }
    
    private RawMessagesBuffer (int bufferId, int messageCount, int[] messageLengths, byte[][] messagesData) {
        this.bufferId = bufferId;
        this.messageCount = messageCount;
        this.messageLengths = messageLengths;
        this.messagesData = messagesData;
    }
    
    /**
     * Converts the buffer into raw binary which can be read with {@link #readFromStream(DataInputStream)}
     */
    public byte[] getSerializedForm () {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOutStream);
        
        // See class javadoc for information on the serialized form
        try {
            dataOut.writeInt(SERIAL_VERSION);
            dataOut.writeInt(bufferId);
            dataOut.writeInt(messageCount);
            for (int length : messageLengths) dataOut.writeInt(length);
            for (byte[] message : messagesData) dataOut.write(message);
        } catch (IOException e) {
            throw new RuntimeException("Exception writing to DataOutputStream: " + e.getMessage());
        }
        
        return byteOutStream.toByteArray();
    }
    
}