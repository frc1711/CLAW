package claw.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import claw.internal.rct.network.low.Waiter;
import claw.internal.rct.network.low.Waiter.NoValueReceivedException;
import claw.internal.rct.network.messages.LogDataMessage;
import claw.internal.rct.network.messages.LogDataMessage.LogData;
import claw.internal.rct.remote.RCTServer;

public class CLAWLogger {
    
    // STATIC
    
    private static final Set<String> usedLogNames = new HashSet<String>();
    private static final List<LogData> logDataBuffer = new ArrayList<LogData>();
    
    private static final Thread dataSenderThread = new Thread(CLAWLogger::dataSenderThreadRunnable);
    private static final Waiter<RCTServer> dataSenderThreadServerWaiter = new Waiter<RCTServer>();
    
    private static boolean isClosed = false;
    
    static {
        dataSenderThread.start();
    }
    
    private static void addData (LogData data) {
        synchronized (logDataBuffer) {
            logDataBuffer.add(data);
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
    
    public static void sendData (RCTServer server) {
        if (isClosed) return;
        dataSenderThreadServerWaiter.receive(server);
    }
    
    public static CLAWLogger getLog (String name) {
        if (usedLogNames.contains(name))
            throw new LogNameConflict(name);
        usedLogNames.add(name);
        return new CLAWLogger(name, CLAWLogger::addData);
    }
    
    public static CLAWLogger getSysLog (String name) {
        return getLog("#"+name);
    }
    
    public static class LogNameConflict extends RuntimeException {
        public LogNameConflict (String name) {
            super("The log name '"+name+"' is already in use.");
        }
    }
    
    // INSTANCE
    
    private final String name;
    private final Consumer<LogData> logDataSender;
    
    private CLAWLogger (String name, Consumer<LogData> logDataSender) {
        this.name = name;
        this.logDataSender = logDataSender;
    }
    
    public String getName () {
        return name;
    }
    
    public void out (String message) {
        logDataSender.accept(new LogData(name, message, false));
    }
    
    public void err (String message) {
        logDataSender.accept(new LogData(name, message, true));
    }
    
}
