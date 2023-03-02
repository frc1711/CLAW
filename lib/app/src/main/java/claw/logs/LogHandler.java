package claw.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import claw.rct.network.low.concurrency.Waiter;
import claw.rct.network.low.concurrency.Waiter.NoValueReceivedException;
import claw.rct.network.messages.LogDataMessage;
import claw.rct.network.messages.LogDataMessage.LogData;
import claw.rct.remote.RCTServer;

public class LogHandler {
    
    private static final int MAX_BUFFER_LENGTH = 250;
    
    private static LogHandler instance = null;
    
    public static LogHandler getInstance () {
        if (instance == null)
            instance = new LogHandler();
        return instance;
    }
    
    private final HashSet<String> registeredLogNames = new HashSet<>();
    private final List<LogData> logDataBuffer = new ArrayList<LogData>();
    
    private final Thread dataSenderThread = new Thread(this::dataSenderThreadRunnable);
    private final Waiter<RCTServer> dataSenderThreadServerWaiter = new Waiter<RCTServer>();
    
    private final HashSet<String> watchingLogNames = new HashSet<>();
    private boolean isClosed = false;
    private boolean watchAllLogs = true;
    
    private LogHandler () {
        dataSenderThread.start();
    }
    
    /**
     * Prepares to send some given {@link LogData} if its log is currently being watched.
     * @param data The {@code LogData} to prepare to send.
     */
    public void addData (LogData data) {
        if (isWatchingLog(data.logName)) {
            synchronized (logDataBuffer) {
                if (logDataBuffer.size() >= MAX_BUFFER_LENGTH)
                    logDataBuffer.remove(0);
                logDataBuffer.add(data);
            }
        }
    }
    
    /**
     * Send all the prepared data to the driver station using a provided {@link RCTServer}.
     * @param server The {@code RCTServer} to use to send data to the client.
     */
    public void sendData (RCTServer server) {
        if (isClosed) return;
        dataSenderThreadServerWaiter.receive(server);
    }
    
    public void watchLogName (String name) {
        synchronized (watchingLogNames) {
            watchingLogNames.add(name);
        }
    }
    
    public void stopWatchingLogs () {
        watchAllLogs = false;
        synchronized (watchingLogNames) {
            watchingLogNames.clear();
        }
    }
    
    public void watchAllLogs () {
        watchAllLogs = true;
    }
    
    public void registerLogName (String name) {
        registeredLogNames.add(name);
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> getRegisteredLogNames () {
        return (HashSet<String>)registeredLogNames.clone();
    }
    
    public boolean isWatchingLog (String logName) {
        if (watchAllLogs) return true;
        synchronized (watchingLogNames) {
            return watchingLogNames.contains(logName);
        }
    }
    
    /**
     * The runnable executed by the data sender thread.
     */
    private void dataSenderThreadRunnable () {
        while (!isClosed) {
            RCTServer server;
            
            // Wait until a server is received to send the data to
            try {
                server = dataSenderThreadServerWaiter.waitForValue();
            } catch (NoValueReceivedException e) {
                continue;
            }
            
            // Get the log data to send based on all available log data in the buffer
            LogData[] logDataToSend;
            synchronized (logDataBuffer) {
                logDataToSend = logDataBuffer.toArray(new LogData[0]);
            }
            
            // Skip the remaining steps (sending the data) if there is no data to send
            if (logDataToSend.length == 0) continue;
            
            // Send the data message
            try {
                // Try to send the data message
                server.sendLogDataMessage(new LogDataMessage(logDataToSend));
                
                // If no IOException was thrown, the message was sent, so clear out all the messages just sent
                // from the logDataBuffer
                synchronized (logDataBuffer) {
                    for (int i = 0; i < logDataToSend.length; i ++)
                        logDataBuffer.remove(0);
                }
            } catch (IOException e) {
                // If an IOException was thrown, nothing happens. Next time the LogDataHandler is notified,
                // the log data will be sent (as the buffer is only cleared if no IOException was thrown)
            }
        }
    }
    
}
