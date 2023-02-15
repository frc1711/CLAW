package claw.rct.network.messages;

import claw.rct.commands.CommandProcessor.HelpMessage;
import claw.rct.network.low.ResponseMessage;

public class CommandsListingMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 1L;
    
    public final HelpMessage[] helpMessages;
    
    public CommandsListingMessage (HelpMessage[] helpMessages) {
        this.helpMessages = helpMessages;
    }
    
}
