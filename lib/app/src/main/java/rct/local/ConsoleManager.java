package rct.local;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * An abstract class managing input from and output to the console. The class is abstract mainly so that
 * ANSI handling can be done elsewhere (which requires a virtual terminal because Windows doesn't support
 * ANSI by default).
 * TODO: Get rid of the seperate driverstation client project and compile the driverstation client jar + batch script with the rest of the library.
 */
public abstract class ConsoleManager {
    
    private final BufferedInputStream inputStream;
    
    public ConsoleManager () {
        inputStream = new BufferedInputStream(System.in);
    }
    
    /**
     * When the user hits enter while a command is running, the input buffer will fill up. Normally,
     * if enter is hit while a command is running, text inputted before it can be processed as another command
     * immediately after the current command finishes. This can be annoying. Use this method after a command
     * finishes to clear any potentially waiting input and not display it to the console.<br />
     * <br />
     * There are two known issues, however. If scrolled down more than one full window's height in the command prompt,
     * calling this method can add several newlines. Generally this is better than not calling this command,
     * but it can still be annoying. The cause of this issue is unknown. The second issue is that text typed after
     * the last enter was pressed WILL NOT be cleared by this method. I believe this is because
     * {@code BufferedInputStream.available()} will return zero if enter was not pressed (where the buffered input
     * stream reads from {@code System.in}).
     */
    public void clearWaitingInputLines () {
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
     * Read a single line of input from the console.
     */
    public String readInputLine () {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int lastByte = 0;
        
        while (lastByte != (int)'\n') {
            try {
                lastByte = inputStream.read();
                if (lastByte == -1) throw new EOFException();
            } catch (IOException e) {
                return "";
            }
            bytes.write(lastByte);
        }
        
        return bytes.toString();
    }
    
    /**
     * Returns {@code true} if there is user input waiting to be processed by the console.
     * This is useful for detecting if a user has hit enter in order to exit some continuously running
     * command.
     */
    public boolean hasInputReady () {
        try {
            return inputStream.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Move up a given number of rows in the console (also moving to the first column).
     */
    public abstract void moveUp (int lines);
    
    /**
     * Clear all text in the current row in the console.
     */
    public abstract void clearLine ();
    
    /**
     * Save the cursor position so that it can be restored later with {@link ConsoleManager#restoreCursorPos()}.
     */
    public abstract void saveCursorPos ();
    
    /**
     * Restore the cursor position from the last {@link ConsoleManager#saveCursorPos()} call.
     */
    public abstract void restoreCursorPos ();
    
    /**
     * Print white text to the console with no newline.
     */
    public abstract void print (String msg);
    
    /**
     * Print white text to the console with a trailing newline.
     */
    public void println (String msg) {
        print(msg + "\n");
    }
    
    /**
     * Print red text to the console with no newline. This is not the same
     * as printing to {@code System.err}.
     */
    public abstract void printErr (String msg);
    
    /**
     * Print red text to the console with a trailing newline. This is not the same
     * as printing to {@code System.err}.
     */
    public void printlnErr (String msg) {
        printErr(msg + "\n");
    }
    
    /**
     * Print yellow/orange system text to the console with no newline.
     */
    public abstract void printSys (String msg);
    
    /**
     * Print yellow/orange system text to the console with a trailing newline.
     */
    public void printlnSys (String msg) {
        printSys(msg + "\n");
    }
    
    /**
     * Flush the console output.
     */
    public void flush () {
        System.out.flush();
    }
    
    /**
     * Clear the console of all output.
     */
    public abstract void clear ();
    
}
