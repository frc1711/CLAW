package rct.network.messages;

import rct.network.low.ResponseMessage;


/**
 * A {@link Message} object which describes output from the remote command interpreter.
 * @see {@link CommandInputMessage}
 */
public class CommandOutputMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 1L;
    
    public final boolean isError;
    public final int inputCmdMsgId;
    public final String commandOutput;
    
    /**
     * Constructs a new {@link CommandOutputMessage}.
     * @param isError       Whether or not the {@code commandOutput} should be treated as an error message instead of normal output.
     * @param inputCmdMsgId The ID of the {@link CommandInputMessage} which was processed to yield this output message.
     * @param commandOutput The output of the command, or an error message if {@code isError} is {@code true}.
     */
    public CommandOutputMessage (boolean isError, int inputCmdMsgId, String commandOutput) {
        this.isError = isError;
        this.inputCmdMsgId = inputCmdMsgId;
        this.commandOutput = commandOutput;
    }
    
}
