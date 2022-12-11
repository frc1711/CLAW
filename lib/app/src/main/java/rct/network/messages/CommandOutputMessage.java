package rct.network.messages;

import rct.network.low.ResponseMessage;

public class CommandOutputMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 1L;
    
    public final boolean isError;
    public final int inputCmdMsgId;
    public final String commandOutput;
    
    public CommandOutputMessage (boolean isError, int inputCmdMsgId, String commandOutput) {
        this.isError = isError;
        this.inputCmdMsgId = inputCmdMsgId;
        this.commandOutput = commandOutput;
    }
    
}
