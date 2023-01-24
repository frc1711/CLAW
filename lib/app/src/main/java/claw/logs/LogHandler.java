package claw.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import claw.logs.LoggerDomain.InvalidLoggerDomainException;
import claw.rct.network.low.Waiter;
import claw.rct.network.low.Waiter.NoValueReceivedException;
import claw.rct.network.messages.LogDataMessage;
import claw.rct.network.messages.LogDataMessage.LogData;
import claw.rct.remote.RCTServer;

public class LogHandler {
    
    private static LogHandler instance = null;
    
    public static LogHandler getInstance () {
        if (instance == null)
            instance = new LogHandler();
        return instance;
    }
    
    private final HashSet<String> registeredLogDomains = new HashSet<>();
    private final List<LogData> logDataBuffer = new ArrayList<LogData>();
    
    private final Thread dataSenderThread = new Thread(this::dataSenderThreadRunnable);
    private final Waiter<RCTServer> dataSenderThreadServerWaiter = new Waiter<RCTServer>();
    
    private boolean isClosed = false;
    
    private final LoggerDomain rootLoggerDomain = new LoggerDomain();
    private boolean logAllDomains = true;
    
    private LogHandler () {
        dataSenderThread.start();
    }
    
    /**
     * Prepares to send some given {@link LogData} if its logger domain is currently being watched.
     * @param data The {@code LogData} to prepare to send.
     */
    public void addData (LogData data) {
        if (isDomainWatched(data.logDomain)) {
            synchronized (logDataBuffer) {
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
    
    /**
     * Set a logger domain string (like {@code "subsystems.swerve.frontLeft.drive"}, for example) as
     * being watched. Logs from watched domains will be sent to the driverstation, while unwatched domains will
     * be ignored.
     * This should be called only in response to feedback from the driverstation.
     * @param domain The logger domain string. Only alphabetic characters and periods are allowed.
     * This string should follow the conventions for java package naming.
     * @throws InvalidLoggerDomainException If the logger domain string is invalid (not alphabetic or
     * a term is empty).
     */
    public void watchDomain (String domain) throws InvalidLoggerDomainException {
        rootLoggerDomain.addDomainPath(domain);
    }
    
    /**
     * Unset all logger domains being watched so that no log domains are sent to the driverstation.
     * Logs from watched domains will be sent to the driverstation, while
     * unwatched domains will be ignored.
     * This should be called only in response to feedback from the driverstation.
     */
    public void unsetWatchedDomains () {
        logAllDomains = false;
        rootLoggerDomain.clearSubdomains();
    }
    
    /**
     * Set all logger domains to be watched so that all log domains are sent to the driverstation.
     * This should be called only in response to feedback from the driverstation.
     */
    public void watchAllDomains () {
        logAllDomains = true;
    }
    
    /**
     * Adds a logger domain to a list of registed domains, indicating that a logger exists with the given domain.
     * @param domain The logger's domain (e.g. {@code "subsystems.swerve.frontLeft.drive"}).
     */
    public void registerDomain (String domain) {
        registeredLogDomains.add(domain);
    }
    
    /**
     * Get a set of all registered logger domains. A registered logger domain is a logger domain belonging to an existing
     * {@link claw.CLAWLogger}.
     * @return The {@code Set<String>} of logger domains used by all existing {@code CLAWLogger}s.
    */
    @SuppressWarnings("unchecked")
    public Set<String> getRegisteredDomains () {
        return (HashSet<String>)registeredLogDomains.clone();
    }
    
    /**
     * Returns whether or not a 
     * @param domain
     * @return
     */
    public boolean isDomainWatched (String domain) {
        return logAllDomains || rootLoggerDomain.doesDomainPathExist(domain);
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
