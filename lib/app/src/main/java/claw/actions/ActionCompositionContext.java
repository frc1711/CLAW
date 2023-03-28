package claw.actions;

import java.util.Optional;

class ActionCompositionContext {
        
    private boolean isTerminated = false;
    private Optional<Action> runningAction = Optional.empty();
    
    public void run (Action action) {
        throwOnTerminate();
        runningAction = Optional.of(action);
        action.run();
    }
    
    private void throwOnTerminate () {
        if (isTerminated) {
            throw new ActionCompositionCanceledException();
        }
    }
    
    public void terminate () {
        isTerminated = true;
        runningAction.ifPresent(Action::cancel);
    }
    
    private class ActionCompositionCanceledException extends RuntimeException {
        public ActionCompositionCanceledException () {
            super("Action composition has been canceled (normal behavior), exit the composition as soon as possible");
        }
    }
    
}
