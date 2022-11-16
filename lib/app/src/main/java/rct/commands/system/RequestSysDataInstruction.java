package rct.commands.system;

import rct.low.InstructionMessage;

public class RequestSysDataInstruction extends InstructionMessage {
    
    public final String[] sysDataPaths;
    
    public RequestSysDataInstruction (int id, String[] sysDataPaths) {
        super(id);
        this.sysDataPaths = sysDataPaths;
    }
    
}
