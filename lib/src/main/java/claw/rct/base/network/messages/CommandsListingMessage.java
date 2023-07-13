package claw.rct.base.network.messages;

import claw.rct.base.commands.CommandProcessor.HelpMessage;
import claw.rct.base.network.low.ResponseMessage;

public class CommandsListingMessage extends ResponseMessage {
    
    public static final long serialVersionUID = 1L;
    
    public final HelpMessage[] helpMessages;
    
    public CommandsListingMessage (HelpMessage[] helpMessages) {
        // TODO: Consider migrating this to networktables because of its one-time-use
        this.helpMessages = helpMessages;
    }
    
}
