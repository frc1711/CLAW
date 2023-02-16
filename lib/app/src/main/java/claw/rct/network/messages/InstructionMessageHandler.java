package claw.rct.network.messages;

import claw.rct.network.low.InstructionMessage;
import claw.rct.network.messages.commands.CommandInputMessage;
import claw.rct.network.messages.commands.ProcessKeepaliveLocal;
import claw.rct.network.messages.commands.StartCommandMessage;

public interface InstructionMessageHandler {
    
    public default void receiveMessage (InstructionMessage msg) {
        if (msg instanceof RequestCommandsListingMessage)
            receiveRequestCommandsListingMessage((RequestCommandsListingMessage)msg);
        
        if (msg instanceof ConnectionCheckMessage)
            receiveConnectionCheckMessage((ConnectionCheckMessage)msg);
        
        if (msg instanceof StartCommandMessage)
            receiveStartCommandMessage((StartCommandMessage)msg);
        
        if (msg instanceof CommandInputMessage)
            receiveCommandInputMessage((CommandInputMessage)msg);
        
        if (msg instanceof ProcessKeepaliveLocal)
            receiveKeepaliveMessage((ProcessKeepaliveLocal)msg);
    }
    
    public void receiveRequestCommandsListingMessage (RequestCommandsListingMessage msg);
    public void receiveConnectionCheckMessage (ConnectionCheckMessage msg);
    public void receiveStartCommandMessage (StartCommandMessage msg);
    public void receiveCommandInputMessage (CommandInputMessage msg);
    public void receiveKeepaliveMessage (ProcessKeepaliveLocal msg);
    
}
