package rct.low;

import java.util.HashMap;

public abstract class ResponseMessage extends Message {
    
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
    
}
