package claw.internal.rct.local.console;

import java.util.function.Consumer;

/**
 * This class is used by {@link InputManager} for filtering out focus tracking ANSI codes
 * from the standard input.
 */
public class FocusTracker {
    
    // The ansi code \x1b[O is sent when the terminal loses focus, \x1b[I is sent when focus is gained
    private static final char
        ENTER_CONTROL_MODE = '\u001b',
        LOSE_FOCUS = 'O',
        GAIN_FOCUS = 'I';
    
    private static final int CODE_LENGTH = 3;
    
    private final Consumer<Character> userInputConsumer;
    
    /**
     * {@code true} if the terminal has focus, {@code false} otherwise
     */
    private boolean focusStatus = true;
    
    /**
     * 0 means no ENTER_CONTROL_MODE has been received, [1, CODE_LENGTH) means the code has been partially read
     * (controlIndex describes the number of control characters read).
     */
    private int controlIndex = 0;
    
    /**
     * Construct a new focus tracker, with a callback for any input that isn't used for focus tracking.
     */
    public FocusTracker (Consumer<Character> userInputConsumer) {
        this.userInputConsumer = userInputConsumer;
    }
    
    /**
     * Receive input from the terminal and process it for focus tracking or send it to the user input callback.
     */
    public synchronized void receiveInput (char in) {
        // If the control index is at 0 and the character isn't ENTER_CONTROL_MODE,
        // it should be processed as user input
        if (controlIndex == 0 && in != ENTER_CONTROL_MODE) {
            userInputConsumer.accept(in);
        } else controlIndex ++; // Otherwise, the control index should be incremented
        
        // If the code length has been reached, then reset controlIndex and set
        // the focus status based on the control character
        if (controlIndex == CODE_LENGTH) {
            controlIndex = 0;
            setFocusCharacter(in);
        }
    }
    
    private void setFocusCharacter (char c) {
        if (c == LOSE_FOCUS)
            focusStatus = false;
        else if (c == GAIN_FOCUS)
            focusStatus = true;
    }
    
    /**
     * Check whether the console has focus, according to the latest focus tracking ANSI control codes.
     */
    public boolean hasFocus () {
        return focusStatus;
    }
    
}
