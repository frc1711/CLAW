package claw.logs;

import claw.rct.base.network.messages.LogDataMessage.LogData;

public class CLAWLogger {
    
    public static CLAWLogger getLogger (String name) {
        LogHandler.getInstance().registerLogName(name);
        return new CLAWLogger(name);
    }
    
    private final String name;
    
    private CLAWLogger (String name) {
        this.name = name;
    }
    
    public String getName () {
        return name;
    }
    
    public void out (String message) {
        LogHandler.getInstance().addData(new LogData(name, message, false));
    }
    
    public void err (String message) {
        LogHandler.getInstance().addData(new LogData(name, message, true));
    }
    
}
