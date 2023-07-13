package claw.rct.base.network.messages.commands;

import claw.rct.base.network.low.InstructionMessage;

/**
 * A {@link InstructionMessage} object which describes an input command line to be processed
 * by the remote command interpreter as starting a new command. When received by remote,
 * if a command is currently running it should be exited and this new command should be
 * processed instead.
 */
public class StartCommandMessage extends InstructionMessage {
    
    public static final long serialVersionUID = 1L;
    
    /**
     * This should be unique to this command sent in this session.
     */
    public final int commandProcessId;
    public final String command;
    
    /**
     * Constructs a new {@link StartCommandMessage} with a unique ID and a
     * command line.
     * @param id        An ID unique to this {@code StartCommandMessage} for this session.
     * @param command   The command line to be processed by the remote command interpreter.
     */
    public StartCommandMessage (int id, String command) {
        this.commandProcessId = id;
        this.command = command;
    }
    
}
