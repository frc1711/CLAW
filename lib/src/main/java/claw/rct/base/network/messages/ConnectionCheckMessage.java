package claw.rct.base.network.messages;

import claw.rct.base.network.low.InstructionMessage;

/**
 * A message sent from local to remote to check whether the server is responding.
 * @see ConnectionResponseMessage
 */
public class ConnectionCheckMessage extends InstructionMessage {
    public static final long serialVersionUID = 1L;
}
