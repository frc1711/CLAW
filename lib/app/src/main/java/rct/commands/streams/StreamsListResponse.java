package rct.commands.streams;

import rct.low.ResponseMessage;

public class StreamsListResponse extends ResponseMessage {
    
    public final String[] streams;
    
    public StreamsListResponse (int id, String[] streams) {
        super(id);
        this.streams = streams;
    }
    
}
