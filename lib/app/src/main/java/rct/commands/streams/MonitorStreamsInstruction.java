package rct.commands.streams;

import rct.low.InstructionMessage;

public class MonitorStreamsInstruction extends InstructionMessage {
    
    public final String[] streamNames;
    
    public MonitorStreamsInstruction (int id, String[] streamNames) {
        super(id);
        this.streamNames = streamNames;
    }
    
}
