package claw.api.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import claw.internal.rct.network.low.Waiter;
import claw.internal.rct.network.low.Waiter.NoValueReceivedException;
import claw.internal.rct.network.messages.StreamDataMessage;
import claw.internal.rct.network.messages.StreamDataMessage.StreamData;
import claw.internal.rct.remote.RCTServer;

public class LogHandler {
    
    private static final Set<String> usedLogNames = new HashSet<String>();
    private static final List<StreamData> streamDataBuffer = new ArrayList<StreamData>();
    
    private static final Thread dataSenderThread = new Thread(LogHandler::dataSenderThreadRunnable);
    private static final Waiter<RCTServer> dataSenderThreadServerWaiter = new Waiter<RCTServer>();
    
    private static boolean isClosed = false;
    
    static {
        dataSenderThread.start();
    }
    
    private LogHandler () { }
    
    private static void addData (StreamData data) {
        synchronized (streamDataBuffer) {
            streamDataBuffer.add(data);
        }
    }
    
    private static void dataSenderThreadRunnable () {
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
                server.sendStreamDataMessage(new StreamDataMessage(streamDataToSend));
                
                // If no IOException was thrown, the message was sent, so clear out all the messages just sent
                // from the streamDataBuffer
                synchronized (streamDataBuffer) {
                    for (int i = 0; i < streamDataToSend.length; i ++)
                        streamDataBuffer.remove(0);
                }
            } catch (IOException e) {
                // If an IOException was thrown, nothing happens. Next time the StreamDataHandler is notified,
                // the stream data will be sent (as the buffer is only cleared if no IOException was thrown)
            }
        }
    }
    
    public static void sendData (RCTServer server) {
        if (isClosed) return;
        dataSenderThreadServerWaiter.receive(server);
    }
    
    public static RCTLog getLog (String name) {
        if (usedLogNames.contains(name))
            throw new LogNameConflict(name);
        usedLogNames.add(name);
        return new RCTLog(name, LogHandler::addData);
    }
    
    public static RCTLog getSysLog (String name) {
        return getLog("#"+name);
    }
    
    public static class LogNameConflict extends RuntimeException {
        public LogNameConflict (String name) {
            super("The log name '"+name+"' is already in use.");
        }
    }
    
}
