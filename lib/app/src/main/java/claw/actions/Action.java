package claw.actions;

import edu.wpi.first.wpilibj2.command.Command;

@FunctionalInterface
public interface Action {
    
    public static Action fromCommand (Command command) {
        return new CommandExecutor(command)::execute;
    }
    
    public void run ();
    
}
