package claw.rct.network.messages;

import java.io.Serializable;

import claw.rct.network.low.ResponseMessage;

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
        
        public static final long serialVersionUID = 4L;
        
        public final String logName, data;
        
        public final boolean isError;
        
        /**
         * Constructs a new {@link LogData} object.
         * @param logName       The name of the log with which the data is associated.
         * @param data          The log data to send.
         */
        public LogData (String logName, String data, boolean isError) {
            this.logName = logName;
            this.data = data;
            this.isError = isError;
        }
        
    }
    
}
