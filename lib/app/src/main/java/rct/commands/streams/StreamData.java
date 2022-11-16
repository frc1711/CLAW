package rct.commands.streams;

import java.io.Serializable;

public class StreamData implements Serializable {
    
    public final String streamName, message;
    public final Type type;
    
    public StreamData (String streamName, Type type, String message) {
        this.streamName = streamName;
        this.type = type;
        this.message = message;
    }
    
    public static enum Type {
        ERROR,
        WARNING,
        DEBUG,
    }
    
}
