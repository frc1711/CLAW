package rct.low;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Represents a low-level message sent from the driverstation or the robot.
 */
public class DataMessage {
    
    /**
     * A serial version which should update whenever the format of DataMessage is updated
     */
    public static final int SERIAL_VERSION = 0xBA5E_0001;
    
    public final MessageType type;
    // If the type is MessageType.RESPONSE, then the ID must match the ID of the instruction it is responding to
    public final int id;
    // dataLength stores the length of dataString (UTF-8) in bytes
    public final int dataLength;
    public final String dataString;
    
    public DataMessage (MessageType type, int id, String dataString) {
        this.type = type;
        this.id = id;
        this.dataString = dataString;
        this.dataLength = dataString.length();
    }
    
    public void putBytesTo (OutputStream out) throws MalformedMessageException {
        final DataOutputStream outputStream = new DataOutputStream(out);
        
        try {
            outputStream.writeInt(SERIAL_VERSION);
            outputStream.writeByte(type.SERIAL_REPR);
            outputStream.writeInt(id);
            outputStream.writeInt(dataLength);
            outputStream.write(dataString.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MalformedMessageException("Error in writing DataMessage to stream: " + e.getMessage());
        }
    }
    
    public static DataMessage readFrom (InputStream in) throws MalformedMessageException {
        final DataInputStream inputStream = new DataInputStream(in);
        
        try {
            final int serialVersion = inputStream.readInt();
            if (serialVersion != SERIAL_VERSION)
                throw new MalformedMessageException(
                    "Incompatible DataMessage serial version, " +
                    "check the RCT versions of the client and robot");
            
            final byte typeId = inputStream.readByte();
            final MessageType type = MessageType.getMessageType(typeId);
            
            final int id = inputStream.readInt();
            final int dataLength = inputStream.readInt();
            String dataString = "";
            
            if (dataLength > 0) {
                final byte[] strBytes = new byte[dataLength];
                final int bytesRead = inputStream.read(strBytes);
                if (bytesRead == -1)
                    throw new MalformedMessageException("Could not read expected number of bytes from DataMessage");
                dataString = new String(strBytes, StandardCharsets.UTF_8);
            }
            
            return new DataMessage(type, id, dataString);
        } catch (EOFException e) {
            throw new MalformedMessageException("Could not read expected number of bytes from DataMessage");
        } catch (IOException e) {
            throw new MalformedMessageException(e.getMessage());
        }
    }
    
    public static enum MessageType {
        /**
         * An instruction message, sent from the driverstation to the robot.
         */
        INSTRUCTION (0xAC),
        
        /**
         * A response message, sent from the robot to the driverstation.
         */
        RESPONSE (0xDF);
        
        /**
         * The binary serialization of the message type. 
         */
        public final byte SERIAL_REPR;
        private MessageType (int serialRepr) {
            this.SERIAL_REPR = (byte)serialRepr;
        }
        
        private static MessageType getMessageType (byte b) throws MalformedMessageException {
            for (MessageType type : MessageType.values())
                if (type.SERIAL_REPR == b) return type;
            throw new MalformedMessageException("Nonexistent MessageType code");
        }
    }
    
    public static class MalformedMessageException extends Exception {
        public MalformedMessageException (String message) {
            super(message);
        }
    }
    
}