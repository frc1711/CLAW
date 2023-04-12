package claw.rct.network.messages.commands;

import claw.rct.network.low.ResponseMessage;

/**
 * A message repeatedly sent from remote to local to indicate that the command is still running and should not
 * be terminated due to inactivity or a lack of response.
 */
public class ProcessKeepaliveRemote extends ResponseMessage {
    public static final long serialVersionUID = 1L;
}
