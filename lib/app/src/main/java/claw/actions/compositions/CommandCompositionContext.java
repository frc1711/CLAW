package claw.actions.compositions;

import claw.actions.Action;
import claw.actions.DelayAction;
import claw.actions.FunctionalCommandBase;
import edu.wpi.first.wpilibj2.command.Command;

public class CommandCompositionContext <CTX extends CommandCompositionContext<?>> extends CompositionContext<CTX> {
    
    public void run (Command command) throws TerminatedContextException {
        executeOperation(Action.fromCommand(command));
    }
    
    public <T> T runGet (FunctionalCommandBase<T> command) throws TerminatedContextException {
        run(command);
        return command.getValue();
    }
    
    public void delay (double durationSecs) throws TerminatedContextException {
        executeOperation(new DelayAction(durationSecs));
    }
    
}
