package claw.actions.compositions;

public interface Context <CTX extends Context<CTX>> {
    
    public default void useContext () throws TerminatedContextException {
        if (isTerminated()) {
            throw new TerminatedContextException();
        }
    }
    
    public boolean isTerminated ();
    public void terminate ();
    
    public static class TerminatedContextException extends Exception {
        public TerminatedContextException () {
            super("The context in which this operation has been performed was terminated.");
        }
    }
    
}
