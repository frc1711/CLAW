package claw.actions.compositions;

import java.util.Optional;
import java.util.function.Supplier;

import claw.actions.Action;

public class CompositionContext <CTX extends CompositionContext<?>> implements Context<CTX> {
    
    private boolean terminated = false;
    
    private Optional<Action> lastAction = Optional.empty();
    private final Object lastActionLock = new Object();
    
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
        
        // TODO: This may fail sometimes? I'm not sure
        operation.runOnContext((CTX)this);
        
        useContext();
    }
    
    public static <CTX extends Context<?>> Action compose (
        Supplier<CTX> contextSupplier,
        Operation<CTX> operation
    ) {
        return new Action() {
            
            private Optional<CTX> context = Optional.empty();
            
            @Override
            public void runAction () {
                // Fill the context field
                CTX ctx = contextSupplier.get();
                context = Optional.of(ctx);
                
                // Try to execute the operation, ignoring termination exceptions
                Context.ignoreTermination(() -> operation.runOnContext(ctx));
                
            }
            
            @Override
            public void cancelRunningAction () {
                context.ifPresent(Context::terminate);
            }
            
        };
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
