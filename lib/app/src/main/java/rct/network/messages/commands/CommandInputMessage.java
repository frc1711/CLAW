package rct.network.messages.commands;

import rct.network.low.ConsoleManager;
import rct.network.low.InstructionMessage;

/**
 * A {@link Message} object which describes the current state of the {@link ConsoleManager},
 * including any input lines requested from {@link CommandOutputMessage} and whether there
 * is input waiting to be processed.
 */
public class CommandInputMessage extends InstructionMessage {
    
    public static final long serialVersionUID = 4L;
    
    /**
     * This should be unique to this command sent in this session.
     */
    public final int commandProcessId;
    
    /**
     * See {@link ConsoleManager#hasInputReady()}.
     */
    public final boolean hasInputReady;
    
    /**
     * An input line from {@link ConsoleManager#readInputLine()}, if an input line was requested.
     * Otherwise, this defaults to {@code null}.
     */
    public final String inputLine;
    
    /**
     * Constructs a new {@link CommandInputMessage}.
     * @param commandProcessId  See {@link CommandInputMessage#commandProcessId}.
     * @param hasInputReady     See {@link CommandInputMessage#hasInputReady}.
     * @param inputLine         See {@link CommandInputMessage#inputLine}.
     */
    public CommandInputMessage (int commandProcessId, boolean hasInputReady, String inputLine) {
        this.commandProcessId = commandProcessId;
        this.hasInputReady = hasInputReady;
        this.inputLine = inputLine;
    }
    
}
