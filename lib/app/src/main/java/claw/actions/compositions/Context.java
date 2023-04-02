package claw.actions.compositions;

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
    
}
