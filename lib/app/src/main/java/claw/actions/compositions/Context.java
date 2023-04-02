package claw.actions.compositions;

import java.util.Optional;

import claw.actions.Action;

public class Context <CTX extends Context<CTX>> {
    
    private boolean terminated = false;
    
    private Optional<Action> lastAction = Optional.empty();
    private final Object lastActionLock = new Object();
    
    public void useContext () throws TerminatedContextException {
        if (isTerminated()) {
            throw new TerminatedContextException();
        }
    }
    
    public void executeOperation (Action actionOperation) throws TerminatedContextException {
        synchronized (lastActionLock) {
            lastAction = Optional.of(actionOperation);
        }
        
        useContext();
        actionOperation.run();
        useContext();
    }
    
    public void executeOperation (Operation<CTX> operation) throws TerminatedContextException {
        synchronized (lastActionLock) {
            lastAction = Optional.empty();
        }
        
        useContext();
        operation.runOnContext(this);
        useContext();
    }
    
    public void terminate () {
        terminated = true;
        synchronized (lastActionLock) {
            lastAction.ifPresent(Action::cancel);
        }
    }
    
    public boolean isTerminated () {
        return terminated;
    }
    
    public static interface Operation <CTX extends Context<CTX>> {
        public void runOnContext (Context<CTX> context) throws TerminatedContextException;
    }
    
    public static class TerminatedContextException extends Exception {
        public TerminatedContextException () {
            super("The context in which this operation has been performed was terminated.");
        }
    }
    
}
