package claw.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import claw.Config;
import claw.Config.ConfigField;
import claw.rct.network.low.Waiter;
import claw.rct.network.low.Waiter.NoValueReceivedException;
import claw.rct.network.messages.StreamDataMessage;
import claw.rct.network.messages.StreamDataMessage.StreamData;
import claw.rct.remote.RCTServer;

public class LogHandler {
    
    private static LogHandler instance;
    
    public static LogHandler getInstance () {
        if (instance == null)
            instance = new LogHandler();
        return instance;
    }
    
    private static final ConfigField<StreamData[]> UNSAVED_STREAM_DATA_CONFIG_FIELD = Config.getInstance().getField("UNSAVED_STREAM_DATA");
    
    private final Set<String> usedStreamNames = new HashSet<String>();
    private final List<StreamData> streamDataBuffer;
    
    private final Thread dataSenderThread;
    private final Waiter<RCTServer> dataSenderThreadServerWaiter = new Waiter<RCTServer>();
    
    private boolean isClosed = false;
    private int nextStreamDataMessageId = 0;
    
    private LogHandler () {
        streamDataBuffer = new ArrayList<StreamData>(Arrays.asList(UNSAVED_STREAM_DATA_CONFIG_FIELD.getValue(new StreamData[0])));
        dataSenderThread = new Thread(this::dataSenderThreadRunnable);
        dataSenderThread.start();
    }
    
    private void addData (StreamData data) {
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
                
                for (StreamData data : streamDataToSend)
                    System.out.println("Sent: " + data.data);
                System.out.println("Sending "+streamDataToSend.length+" message(s)");
                
                // If no IOException was thrown, the message was sent, so clear out all the messages just sent
                // from the streamDataBuffer
                synchronized (streamDataBuffer) {
                    for (int i = 0; i < streamDataToSend.length; i ++)
                        streamDataBuffer.remove(0);
                    
                    // Increment the stream data message ID so the next message has a new ID and won't be ignored
                    nextStreamDataMessageId ++;
                }
            } catch (IOException e) {
                // If an IOException was thrown, nothing happens. Next time the StreamDataHandler is notified,
                // the stream data will be sent (as the buffer is only cleared if no IOException was thrown)
            }
        }
    }
    
    /**
     * Save any data that hasn't yet been sent
     */
    public void saveUnsentData () {
        synchronized (streamDataBuffer) {
            isClosed = true;
            StreamData[] remainingData = streamDataBuffer.toArray(new StreamData[0]);
            UNSAVED_STREAM_DATA_CONFIG_FIELD.setValue(remainingData);
            streamDataBuffer.clear();
        }
    }
    
    public void sendData (RCTServer server) {
        if (isClosed) return;
        dataSenderThreadServerWaiter.receive(server);
    }
    
    public RCTLog getLog (String name) {
        if (usedStreamNames.contains(name))
            throw new LogNameConflict(name);
        usedStreamNames.add(name);
        return new RCTLog(name, this::addData);
    }
    
    public RCTLog getSysLog (String name) {
        return getLog("#"+name);
    }
    
    public static class LogNameConflict extends RuntimeException {
        public LogNameConflict (String name) {
            super("The log name '"+name+"' is already in use.");
        }
    }
    
}
