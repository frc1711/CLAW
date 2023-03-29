package claw.rct.network.low;

import java.util.ArrayList;
import java.util.Arrays;

import claw.actions.compositions.ActionCompositionContext;

/**
 * An interface for managing input from and output to the driverstation console.
 */
public interface ConsoleManager extends ActionCompositionContext {
    
    public static final int MAX_COLS_PER_LINE = 100;
    
    /**
     * Break a long message apart into many lines which fit in the console.
     * @param message           The message to be broken apart into lines.
     * @return                  The formatted message.
     */
    public static String formatMessage (String message) {
        return formatMessage(message, 0);
    }
    
    /**
     * Break a long message apart into many lines which fit in the console, and apply an indent to the beginning of each
     * new outputted line.
     * @param message           The message to be broken apart and indented.
     * @param indent            The number of spaces to indent each line with.
     * @return                  The formatted message.
     */
    public static String formatMessage (String message, int indent) {
        ArrayList<String> inputLines = new ArrayList<>(Arrays.asList(message.split("\n")));
        ArrayList<String> outputLines = new ArrayList<>();
        
        int maxColsAfterIndex = MAX_COLS_PER_LINE - indent;
        
        for (String inputLine : inputLines) {
            if (inputLine.length() == 0)
                outputLines.add("");
            
            while (inputLine.length() > 0) {
                // Determine the length to cut out from the input line
                int nextLineMaxLength = Math.min(inputLine.length(), maxColsAfterIndex);
                
                // Get the line to add to the ouputLines
                String outputLine = inputLine.substring(0, nextLineMaxLength);
                
                // Word breaking:
                // Check if this line will have to be broken apart again after this cut (i.e. there will be another line following outputLine)
                if (inputLine.length() > nextLineMaxLength) {
                    
                    // Check that there is a word continuously between the end of outputLine and the following line
                    if (
                        !Character.isWhitespace(inputLine.charAt(nextLineMaxLength-1)) &&
                        !Character.isWhitespace(inputLine.charAt(nextLineMaxLength))
                    ) {
                        // There is a word running continuously from outputLine into the next, so try to apply a word break
                        // Also, add a space to the end so the space is removed from the beginning of the next line
                        outputLine = applyWordBreak(outputLine) + " ";
                    }
                }
                
                // Remove the outputLine's content from the beginning of the inputLine
                inputLine = inputLine.substring(outputLine.length());
                
                // Add the line to outputLines and apply the indent
                outputLines.add(" ".repeat(indent) + outputLine);
            }
        }
        
        return String.join("\n", outputLines);
    }
    
    private static String applyWordBreak (String inputLine) {
        // Break apart the line into words
        ArrayList<String> words = new ArrayList<>(Arrays.asList(inputLine.split(" ")));
        
        // Remove the last word if there is more than one word
        if (words.size() > 1)
            words.remove(words.size()-1);
        
        // Return the words joined together with spaces
        return String.join(" ", words);
    }
    
    /**
     * Read a single line of input from the console.
     */
    String readInputLine ();
    
    /**
     * Returns {@code true} if there is user input waiting to be processed by the console.
     * This can detect whether the user has hit a key, so that a continuously running command
     * can exit some condition.
     */
    boolean hasInputReady ();
    
    /**
     * Clear any submitted user input that is currently waiting to be processed.
     * This prevents user input typed during some console-blocking operation from
     * appearing again when you wait for their next line of input. 
     */
    void clearWaitingInputLines ();
    
    /**
     * Move up a given number of rows in the console (also moving to the first column).
     */
    void moveUp (int lines);
    
    /**
     * Clear all text in the current row in the console.
     */
    void clearLine ();
    
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
    void print (String msg);
    
    /**
     * Print white text to the console with a trailing newline.
     */
    default void println (String msg) {
        print(msg + "\n");
    }
    
    /**
     * Print red text to the console with no newline. This is not the same
     * as printing to {@code System.err}.
     */
    void printErr (String msg);
    
    /**
     * Print red text to the console with a trailing newline. This is not the same
     * as printing to {@code System.err}.
     */
    default void printlnErr (String msg) {
        printErr(msg + "\n");
    }
    
    /**
     * Print yellow/orange system text to the console with no newline.
     */
    void printSys (String msg);
    
    /**
     * Print yellow/orange system text to the console with a trailing newline.
     */
    default void printlnSys (String msg) {
        printSys(msg + "\n");
    }
    
    /**
     * Flush the console output.
     */
    void flush ();
    
    /**
     * Clear the console of all output.
     */
    void clear ();
    
}
