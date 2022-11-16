package rct.commands.system;

import rct.low.ResponseMessage;

public class SysDataResponse extends ResponseMessage {
    
    public final SysDataFrame[] frames;
    
    public SysDataResponse (int id, SysDataFrame[] frames) {
        super(id);
        this.frames = frames;
    }
    
}
