package rct.low;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public abstract class ResponseMessage {
    
    public static final long serialVersionUID = 1L;
    
    public static final class ConfigList extends ResponseMessage {
        public final String message;
        public ConfigList (String message) {
            this.message = message;
        }
    }
    
    public static final class ConfigSetStatus extends ResponseMessage {
        public final Status status;
        public ConfigSetStatus (Status status) {
            this.status = status;
        }
    }
    
    public static final class DebugInfo extends ResponseMessage {
        public final HashMap<String, String> values;
        public final HashMap<String, DebugInfo> subdomains;
        public DebugInfo (HashMap<String, String> values, HashMap<String, DebugInfo> subdomains) {
            this.values = values;
            this.subdomains = subdomains;
        }
    }
    
    public static final class StreamsList extends ResponseMessage {
        public final String message;
        public StreamsList (String message) {
            this.message = message;
        }
    }
    
    public static final class StreamData extends ResponseMessage {
        public final String message;
        public StreamData (String message) {
            this.message = message;
        }
    }
    
    public static enum Status {
        SUCCESS,
        FAILURE,
    }
    
    public byte[] getData () {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bytesOut);
            out.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException("Exception reading InstructionMessage byte[] data:\n" + e);
        }
        
        return bytesOut.toByteArray();
    }
    
}
