package claw.rct.network.messages;

import claw.rct.network.low.ResponseMessage;
import claw.rct.network.messages.commands.CommandOutputMessage;
import claw.rct.network.messages.commands.ProcessKeepaliveRemote;

public interface ResponseMessageHandler {
    
    public default void receiveMessage (ResponseMessage msg) {
        if (msg instanceof ConnectionResponseMessage)
            receiveConnectionResponseMessage((ConnectionResponseMessage)msg);
        
        if (msg instanceof ProcessKeepaliveRemote)
            receiveProcessKeepaliveMessage((ProcessKeepaliveRemote)msg);
        
        if (msg instanceof CommandOutputMessage)
            receiveCommandOutputMessage((CommandOutputMessage)msg);
        
        if (msg instanceof LogDataMessage)
            receiveLogDataMessage((LogDataMessage)msg);
        
        if (msg instanceof CommandsListingMessage)
            receiveCommandsListingMessage((CommandsListingMessage)msg);
    }
    
    public void receiveConnectionResponseMessage (ConnectionResponseMessage msg);
    public void receiveProcessKeepaliveMessage (ProcessKeepaliveRemote msg);
    public void receiveCommandOutputMessage (CommandOutputMessage msg);
    public void receiveLogDataMessage (LogDataMessage msg);
    public void receiveCommandsListingMessage (CommandsListingMessage msg);
    
}
