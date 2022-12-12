package rct.network.messages;

import java.io.Serializable;

import rct.network.low.ResponseMessage;

public class StreamDataMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 2L;
    
    public final StreamData[] streamData;
    
    public StreamDataMessage (StreamData[] streamData) {
        this.streamData = streamData;
    }
    
    public static class StreamData implements Serializable {
        
        public static final long serialVersionUID = 1L;
        
        public final String streamName, data;
        
        public StreamData (String streamName, String data) {
            this.streamName = streamName;
            this.data = data;
        }
        
    }
    
}
