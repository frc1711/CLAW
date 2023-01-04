package claw.logs;

import java.util.function.Consumer;

import claw.rct.network.messages.StreamDataMessage.StreamData;

public class RCTLog {
        
    private final String name;
    private final Consumer<StreamData> streamDataSender;
    
    RCTLog (String name, Consumer<StreamData> streamDataSender) {
        this.name = name;
        this.streamDataSender = streamDataSender;
    }
    
    public String getName () {
        return name;
    }
    
    public void out (String message) {
        streamDataSender.accept(new StreamData(name, message, false));
    }
    
    public void err (String message) {
        streamDataSender.accept(new StreamData(name, message, true));
    }
    
}
