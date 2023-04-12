package claw.rct.local.console;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;
import org.jline.terminal.Terminal;

/**
 * Used by {@link InputManager} for managing the user input buffer and writing it to the terminal.
 */
public class UserInputWriter {
    
    private final StringBuffer buffer = new StringBuffer();
    private final Terminal terminal;
    
    public UserInputWriter (Terminal terminal) {
        this.terminal = terminal;
    }
    
    /**
     * Add a character to the internal buffer, and optionally write the value to the terminal.
     */
    public synchronized void putChar (char c, boolean write) {
        if (c == '\b') {
            
            // Backspace character received
            
            // Check if there is room to delete a character
            if (buffer.length() > 0) {
                
                // If there is room, delete the character from the buffer and update the terminal
                buffer.deleteCharAt(buffer.length() - 1);
                if (write) {
                    terminal.writer().print(Ansi.ansi().a('\b').eraseLine(Erase.FORWARD));
                }
            }
            
        } else {
            
            // Add the character to the buffer and write it to the terminal
            buffer.append(c);
            if (write) terminal.writer().print(c);
            
        }
    }
    
    /**
     * Write the entire contents of the buffer to the terminal
     */
    public synchronized void writeBufferContents () {
        terminal.writer().print(buffer);
    }
    
    /**
     * Check whether or not the internal buffer contains a full line of text input.
     */
    public boolean hasFullLine () {
        return buffer.toString().indexOf('\n') != -1;
    }
    
    /**
     * Clear the input buffer until the first newline (inclusive), and return the text removed from the buffer.
     * If there is no newline, an empty string will be returned. Note that the closing {@code '\n'} will always be
     * returned (unless there was no newline in the buffer).
     */
    public synchronized String getLine () {
        // Get the index of the newline character in the buffer
        int newlineIndex = buffer.toString().indexOf('\n');
        
        // Do nothing if there is no newline
        if (newlineIndex == -1) return "";
        
        // Retrieve the line from the buffer
        String line = buffer.substring(0, newlineIndex + 1);
        
        // Delete the line from the buffer
        buffer.delete(0, newlineIndex + 1);
        
        // Return the line
        return line;
    }
    
    /**
     * Check whether there is user input waiting.
     */
    public boolean ready () {
        return buffer.length() > 0;
    }
    
    /**
     * Clears the user input buffer.
     */
    public synchronized void clearBuffer () {
        buffer.delete(0, buffer.length());
    }
    
}
