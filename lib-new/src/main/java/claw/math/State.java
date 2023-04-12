package claw.math;

/**
 * An interface for the internal state of some {@link StateMachine}.
 */
public interface State<S extends State<S, I>, I> {
    
    /**
     * Get the next {@link State} given the {@link StateMachine} input {@code input}.
     * @param input The input event or data to some state machine.
     * @return      The state the input leads to.
     */
    public S getNewState (I input);
    
}