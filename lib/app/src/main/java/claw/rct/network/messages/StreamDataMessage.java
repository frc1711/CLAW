package claw.rct.network.messages;

import java.io.Serializable;

import claw.rct.network.low.ResponseMessage;

/**
 * A message sent from remote to local containing {@link StreamData}.
 */
public class StreamDataMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 3L;
    
    public final int messageId;
    public final StreamData[] streamData;
    
    /**
     * Constructs a new {@link StreamDataMessage} given an array of {@link StreamData} to send.
     * @param streamData The {@code StreamData} to send.
     */
    public StreamDataMessage (int streamDataMessageId, StreamData[] streamData) {
        messageId = streamDataMessageId;
        this.streamData = streamData;
    }
    
    /**
     * Represents stream data created by the roboRIO (remote) to be consumed by the driverstation (local). 
     */
    public static class StreamData implements Serializable {
        
        public static final long serialVersionUID = 1L;
        
        public final String streamName, data;
        
        /**
         * Constructs a new {@link StreamData} object.
         * @param streamName    The name of the stream with which the data is associated.
         * @param data          The stream data.
         */
        public StreamData (String streamName, String data) {
            this.streamName = streamName;
            this.data = data;
        }
        
    }
    
}
