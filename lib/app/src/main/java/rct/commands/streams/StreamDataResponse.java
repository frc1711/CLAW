package rct.commands.streams;

import rct.low.ResponseMessage;

public class StreamDataResponse extends ResponseMessage {
    
    public final StreamData[] data;
    
    public StreamDataResponse (int id, StreamData[] data) {
        super(id);
        this.data = data;
    }
    
}
