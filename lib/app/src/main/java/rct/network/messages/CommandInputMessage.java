package rct.network.messages;

import rct.network.low.InstructionMessage;

public class CommandInputMessage extends InstructionMessage {
    
    public static final long serialVersionUID = 1L;
    
    public final int id;
    public final String command;
    
    public CommandInputMessage (int id, String command) {
        this.id = id;
        this.command = command;
    }
    
}
