package rct.low;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a serializable object which accepts a list of Byte arrays and stores a byte[][] buffer
 * representing a list of raw byte[] messages.
 */
public class RawMessagesBuffer implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The ID associated with this buffer, useful for ensuring all messages have been received
     */
    public final int id;
    
    /**
     * The array of byte arrays, representing a list of raw byte messages.
     */
    public final byte[][] buffer;
    
    /**
     * Creates a new RawMessagesBuffer with an id and a list of raw Byte[] messages.
     */
    public RawMessagesBuffer (int id, List<Byte[]> byteArraysBuffer) {
        // Set this RawMessagesBuffer's ID
        this.id = id;
        
        // Create the buffer[][] to store all the byte data
        buffer = new byte[byteArraysBuffer.size()][];
        
        // Loop through the list of Byte[] arrays
        for (int i = 0; i < buffer.length; i ++) {
            // Create the byte[] buffer to store the Byte[] data
            Byte[] byteObjs = byteArraysBuffer.get(i);
            buffer[i] = new byte[byteObjs.length];
            
            // Fill the buffers
            for (int j = 0; j < byteObjs.length; j ++)
                buffer[i][j] = byteObjs[j];
        }
    }
    
}