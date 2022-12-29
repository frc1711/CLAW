package rct.network.low;

/**
 * An interface for managing input from and output to the driverstation console.
 */
public interface ConsoleManager {
    
    /**
     * Read a single line of input from the console.
     */
    public String readInputLine ();
    
    /**
     * Returns {@code true} if there is user input waiting to be processed by the console.
     * This is useful for detecting if a user has hit enter in order to exit some continuously running
     * command.
     */
    public boolean hasInputReady ();
    
    /**
     * Clear any submitted user input that is currently waiting.
     * This is useful to call so that input entered by the
     * user does not affect any input lines read long after.
     */
    public default void clearWaitingInputLines () {
        // Do nothing if no input is ready
        if (!hasInputReady()) return;
        
        // Save the cursor position so we can return when finished
        saveCursorPos();
        
        // Move to a new line so we don't erase the current line
        println("");
        
        // Read all available input lines
        String lines = "";
        while (hasInputReady()) {
            lines += readInputLine();
        }
        
        // Clear all the lines inputted
        for (int i = 0; i < lines.length(); i ++) {
            if (lines.charAt(i) == '\n') {
                moveUp(1);
                clearLine();
            }
        }
        
        // Move the cursor back to the starting position
        restoreCursorPos();
    }
    
    /**
     * Move up a given number of rows in the console (also moving to the first column).
     */
    public void moveUp (int lines);
    
    /**
     * Clear all text in the current row in the console.
     */
    public void clearLine ();
    
    /**
     * Save the cursor position so that it can be restored later with {@link ConsoleManager#restoreCursorPos()}.
     */
    void saveCursorPos ();
    
    /**
     * Restore the cursor position from the last {@link ConsoleManager#saveCursorPos()} call.
     */
    void restoreCursorPos ();
    
    /**
     * Print white text to the console with no newline.
     */
    public void print (String msg);
    
    /**
     * Print white text to the console with a trailing newline.
     */
    public default void println (String msg) {
        print(msg + "\n");
    }
    
    /**
     * Print red text to the console with no newline. This is not the same
     * as printing to {@code System.err}.
     */
    public void printErr (String msg);
    
    /**
     * Print red text to the console with a trailing newline. This is not the same
     * as printing to {@code System.err}.
     */
    public default void printlnErr (String msg) {
        printErr(msg + "\n");
    }
    
    /**
     * Print yellow/orange system text to the console with no newline.
     */
    public void printSys (String msg);
    
    /**
     * Print yellow/orange system text to the console with a trailing newline.
     */
    public default void printlnSys (String msg) {
        printSys(msg + "\n");
    }
    
    /**
     * Flush the console output.
     */
    public void flush ();
    
    /**
     * Clear the console of all output.
     */
    public void clear ();
    
}
