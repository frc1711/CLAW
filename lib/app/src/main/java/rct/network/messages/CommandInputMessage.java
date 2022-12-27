package rct.network.messages;

import rct.network.low.InstructionMessage;

/**
 * A {@link Message} object which describes an input command line to be processed
 * by the remote command interpreter.
 * @see {@link CommandOutputMessage}
 */
public class CommandInputMessage extends InstructionMessage {
    
    public static final long serialVersionUID = 1L;
    
    /**
     * This should be unique to this command sent in this session.
     */
    public final int id;
    public final String command;
    
    /**
     * Constructs a new {@link CommandInputMessage} with a unique ID and a
     * command line.
     * @param id        An ID unique to this {@code CommandInputMessage} for this session.
     * @param command   The command line to be processed by the remote command interpreter.
     */
    public CommandInputMessage (int id, String command) {
        this.id = id;
        this.command = command;
    }
    
}
