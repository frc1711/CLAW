package claw.actions;

import edu.wpi.first.wpilibj2.command.Command;

public interface Action {
    
    public static Action fromCommand (Command command) {
        return new CommandAction(command);
    }
    
    public void run ();
    
}
