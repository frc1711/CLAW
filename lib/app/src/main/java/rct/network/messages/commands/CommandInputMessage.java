package rct.network.messages.commands;

import rct.network.low.ConsoleManager;
import rct.network.low.InstructionMessage;
import rct.network.messages.commands.CommandOutputMessage.ConsoleManagerRequest;

/**
 * A {@link Message} object which describes the current state of the {@link ConsoleManager},
 * including any input lines requested from {@link CommandOutputMessage} and whether there
 * is input waiting to be processed.
 */
public class CommandInputMessage extends InstructionMessage {
    
    public static final long serialVersionUID = 5L;
    
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
     * The {@link ConsoleManagerRequest} this {@link CommandInputMessage} is responding to.
     */
    public final ConsoleManagerRequest request;
    
    /**
     * Constructs a new {@link CommandInputMessage}.
     * @param commandProcessId  See {@link CommandInputMessage#commandProcessId}.
     * @param hasInputReady     See {@link CommandInputMessage#hasInputReady}.
     * @param inputLine         See {@link CommandInputMessage#inputLine}.
     * @param request           See {@link CommandInputMessage#request}.
     */
    public CommandInputMessage (int commandProcessId, boolean hasInputReady, String inputLine, ConsoleManagerRequest request) {
        this.commandProcessId = commandProcessId;
        this.hasInputReady = hasInputReady;
        this.inputLine = inputLine;
        this.request = request;
    }
    
}
