package rct.network.messages;

import rct.network.low.ResponseMessage;

public class StreamDataMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 1L;
    
    public final String streamName, data;
    
    public StreamDataMessage (int id, String streamName, String data) {
        this.streamName = streamName;
        this.data = data;
    }
    
}
