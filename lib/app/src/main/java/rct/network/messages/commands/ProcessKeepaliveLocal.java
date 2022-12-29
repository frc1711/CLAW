package rct.network.messages.commands;

import rct.network.low.InstructionMessage;

/**
 * A message repeatedly sent from local to remote to indicate that the command is still running and should not
 * be terminated due to inactivity or a lack of response.
 */
public class ProcessKeepaliveLocal extends InstructionMessage {
    public static final long serialVersionUID = 1L;
}
