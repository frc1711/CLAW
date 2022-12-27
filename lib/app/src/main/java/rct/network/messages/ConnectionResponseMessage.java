package rct.network.messages;

import rct.network.low.ResponseMessage;

/**
 * A message sent from remote to local to acknowledge a {@link ConnectionCheckMessage},
 * indicating that the server is responsive.
 */
public class ConnectionResponseMessage extends ResponseMessage {
    public static final long serialVersionUID = 1L;
}
