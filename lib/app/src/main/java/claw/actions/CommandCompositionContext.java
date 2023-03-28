package claw.actions;

import edu.wpi.first.wpilibj2.command.Command;

public class CommandCompositionContext extends ActionCompositionContext {
    
    public void run (Command command) {
        run(Action.fromCommand(command));
    }
    
    public <T> T runGet (FunctionalCommandBase<T> command) {
        run(command);
        return command.getValue();
    }
    
    public void delay (double durationSecs) {
        run(new DelayAction(durationSecs));
    }
    
}
