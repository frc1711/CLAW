package claw.internal.rct.network.messages;

import java.io.Serializable;

import claw.internal.rct.network.low.ResponseMessage;

/**
 * A message sent from remote to local containing {@link LogData}.
 */
public class LogDataMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 5L;
    
    public final LogData[] logData;
    
    /**
     * Constructs a new {@link LogDataMessage} given an array of {@link LogData} to send.
     * @param logData The {@code LogData} to send.
     */
    public LogDataMessage (LogData[] logData) {
        this.logData = logData;
    }
    
    /**
     * Represents log data created by the roboRIO (remote) to be consumed by the driverstation (local). 
     */
    public static class LogData implements Serializable {
        
        public static final long serialVersionUID = 3L;
        
        public final String logDomain, data;
        
        public final boolean isError;
        
        /**
         * Constructs a new {@link LogData} object.
         * @param logDomain     The domain of the log with which the data is associated. This should be formatted
         * like a java package (e.g. {@code "subsystems.swerve.frontLeft"})
         * @param data          The log data to send.
         */
        public LogData (String logDomain, String data, boolean isError) {
            this.logDomain = logDomain;
            this.data = data;
            this.isError = isError;
        }
        
    }
    
}
