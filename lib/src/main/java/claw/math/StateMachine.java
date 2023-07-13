package claw.math;

/**
 * A generic state machine which allows for transitioning and storing {@link State}.
 */
public class StateMachine <S extends State<S, I>, I> {
    
    private S currentState;
    
    /**
     * Create a new {@link StateMachine} with some initial state.
     * @param initialState  The initial {@link State} of this state machine.
     */
    public StateMachine (S initialState) {
        currentState = initialState;
    }
    
    /**
     * Transition the state of this {@link StateMachine} according to a given {@code input}.
     * @param input The input which will be used to get a new state.
     * @return      This state machine, so that several transition inputs can easily be chained together.
     */
    public StateMachine<S, I> transition (I input) {
        currentState = currentState.getNewState(input);
        return this;
    }
    
    /**
     * Get the internal {@link State} of this {@link StateMachine}.
     * @return  The internal state.
     */
    public S getState () {
        return currentState;
    }
    
}
