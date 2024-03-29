package claw.rct.network.messages;

import claw.rct.network.low.ResponseMessage;

/**
 * A message sent from remote to local to acknowledge a {@link ConnectionCheckMessage},
 * indicating that the server is responsive.
 */
public class ConnectionResponseMessage extends ResponseMessage {
    public static final long serialVersionUID = 1L;
}
