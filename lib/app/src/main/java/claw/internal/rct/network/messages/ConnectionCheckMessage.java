package claw.internal.rct.network.messages;

import claw.internal.rct.network.low.InstructionMessage;

/**
 * A message sent from local to remote to check whether the server is responding.
 * @see {@link ConnectionResponseMessage}
 */
public class ConnectionCheckMessage extends InstructionMessage {
    public static final long serialVersionUID = 1L;
}
