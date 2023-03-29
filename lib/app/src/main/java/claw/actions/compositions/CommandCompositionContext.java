package claw.actions.compositions;

import claw.actions.Action;
import claw.actions.FunctionalCommandBase;
import claw.actions.compositions.ActionCompositionContext.ActionRunnerContext;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.WaitCommand;

public class CommandCompositionContext extends ActionRunnerContext {
    
    public void run (Command command) {
        runAction(Action.fromCommand(command));
    }
    
    public <T> T runGet (FunctionalCommandBase<T> command) {
        run(command);
        return command.getValue();
    }
    
    public void delay (double durationSecs) {
        run(new WaitCommand(durationSecs));
    }
    
}
