package claw.actions.compositions;

import java.util.Optional;
import java.util.function.Supplier;

import claw.actions.Action;

public interface Context <CTX extends Context<?>> {
    
    public default void useContext () throws TerminatedContextException {
        if (isTerminated()) {
            throw getTerminatedException();
        }
    }
    
    public default TerminatedContextException getTerminatedException () {
        return new TerminatedContextException();
    }
    
    public boolean isTerminated ();
    public void terminate ();
    
    public static class TerminatedContextException extends Exception {
        public TerminatedContextException () {
            super("The context in which this operation has been performed was terminated.");
        }
    }
    
    public static interface Operation <CTX extends Context<?>> {
        public void runOnContext (CTX context) throws TerminatedContextException;
    }
    
    public static interface TerminableExecution {
        public void run () throws TerminatedContextException;
    }
    
    public static void ignoreTermination (TerminableExecution exe) {
        try {
            // TODO: Check this out in case the unchecked cast can fail
            exe.run();
        } catch (TerminatedContextException e) { }
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
    
}
