package claw.streams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import claw.rct.network.low.Waiter;
import claw.rct.network.low.Waiter.NoValueReceivedException;
import claw.rct.network.messages.StreamDataMessage;
import claw.rct.network.messages.StreamDataMessage.StreamData;
import claw.rct.remote.RCTServer;

public class StreamDataHandler {
    
    private final List<StreamData> streamDataBuffer = new ArrayList<StreamData>();
    
    private final Thread dataSenderThread;
    private final Waiter<RCTServer> dataSenderThreadServerWaiter = new Waiter<RCTServer>();
    
    private boolean isClosed = false;
    private int nextStreamDataMessageId = 0;
    
    public StreamDataHandler () {
        dataSenderThread = new Thread(this::dataSenderThreadRunnable);
        dataSenderThread.start();
    }
    
    public void addData (StreamData data) {
        synchronized (streamDataBuffer) {
            streamDataBuffer.add(data);
        }
    }
    
    private void dataSenderThreadRunnable () {
        while (!isClosed) {
            RCTServer server;
            
            // Wait until a server is received to send the data to
            try {
                server = dataSenderThreadServerWaiter.waitForValue();
            } catch (NoValueReceivedException e) {
                continue;
            }
            
            // Get the stream data to send based on all available stream data in the buffer
            StreamData[] streamDataToSend;
            synchronized (streamDataBuffer) {
                streamDataToSend = streamDataBuffer.toArray(new StreamData[0]);
            }
            
            // Skip the remaining steps (sending the data) if there is no data to send
            if (streamDataToSend.length == 0) continue;
            
            // Send the data message
            try {
                // Try to send the data message
                server.sendStreamDataMessage(new StreamDataMessage(nextStreamDataMessageId, streamDataToSend));
                
                // If no IOException was thrown, the message was sent, so clear out all the messages just sent
                // from the streamDataBuffer
                synchronized (streamDataBuffer) {
                    // Clear the number of stream datas successfully sent from the beginning of the buffer
                    // (so they aren't sent twice)
                    streamDataBuffer.subList(0, streamDataToSend.length).clear();
                    
                    // Increment the stream data message ID so the next message has a new ID and won't be ignored
                    nextStreamDataMessageId ++;
                }
            } catch (IOException e) {
                
                System.out.println(e.getMessage());
                
                // If an IOException was thrown, nothing happens. Next time the StreamDataHandler is notified,
                // the stream data will be sent (as the buffer is only cleared if no IOException was thrown)
            }
        }
    }
    
    /**
     * Save any data that hasn't yet been sent
     */
    public void saveUnsentData () {
        
        // TODO: Implement saving unsent stream data
        
        // isClosed = true;
        // StreamData[] streamDataToSave = 
        // synchronized (streamDataBuffer) {
        //     streamDataBuffer.toArray(new StreamData[0]);
        // }
    }
    
    public void sendData (RCTServer server) {
        if (isClosed) return;
        dataSenderThreadServerWaiter.receive(server);
    }
    
}
