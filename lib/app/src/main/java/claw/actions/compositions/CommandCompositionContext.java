package claw.actions.compositions;

import java.util.Optional;

import claw.actions.Action;
import claw.actions.DelayAction;
import claw.actions.FunctionalCommandBase;
import edu.wpi.first.wpilibj2.command.Command;

public class CommandCompositionContext <CTX extends CommandCompositionContext<?>> implements Context<CTX> {
    
    private boolean terminated = false;
    
    private Optional<Action> lastAction = Optional.empty();
    private final Object lastActionLock = new Object();
    
    public <T> T runGet (FunctionalCommandBase<T> command) throws TerminatedContextException {
        run(command);
        return command.getValue();
    }
    
    public void delay (double durationSecs) throws TerminatedContextException {
        runAction(new DelayAction(durationSecs));
    }
    
    public void run (Command command) throws TerminatedContextException {
        runAction(Action.fromCommand(command));
    }
    
    public void runAction (Action action) throws TerminatedContextException {
        synchronized (lastActionLock) {
            lastAction = Optional.of(action);
        }
        
        useContext();
        action.run();
        useContext();
    }
    
    @Override
    public void terminate () {
        terminated = true;
        synchronized (lastActionLock) {
            lastAction.ifPresent(Action::cancel);
        }
    }
    
    @Override
    public boolean isTerminated () {
        return terminated;
    }
    
}
