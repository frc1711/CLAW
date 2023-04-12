package console;

import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;

/**
 * Handles low-level input from the console and line reading. This is necessary because there are three
 * types of inputs which all act differently: user input (read in a relatively normal way), spacebar and
 * return key input from the key hook (necessary to prevent the driver station from disabling the robot
 * while the terminal is being used), and focus-tracking input, a type of ANSI input received which allows
 * the input manager to track whether or not the console window is focused.
 */
public class InputManager {
    
    private final NonBlockingReader reader;
    private final Object charWaiter = new Object();
    private final UserInputWriter userInputWriter;
    
    /**
     * The focus tracker filters out input pertaining to whether or not the console window is focused,
     * and passes user input (all other input from the terminal) to {@link #handleUserInput(char)}.
     */
    private final FocusTracker focusTracker = new FocusTracker(this::handleUserInput);
    
    /**
     * Whether or not the input manager is actively waiting for user input (controls whether or not
     * user input typed into the terminal should be displayed right away, among other things).
     */
    private boolean isReadingInput = false;
    
    public InputManager (Terminal terminal) {
        userInputWriter = new UserInputWriter(terminal);
        this.reader = terminal.reader();
        
        DriverStationDisableKeysHook.installKeyHook(focusTracker::hasFocus, this::onEnterKey, this::onSpaceKey);
        new Thread(this::inputReaderRunnable).start();
        terminal.trackFocus(true);
    }
    
    /**
     * A key hook callback for when the enter key is pressed
     */
    private void onEnterKey () {
        focusTracker.receiveInput('\n');
    }
    
    /**
     * A key hook callback for when the spacebar is pressed
     */
    private void onSpaceKey () {
        focusTracker.receiveInput(' ');
    }
    
    /**
     * A callback passed to the focus tracker which is used whenever input from the terminal
     * is not to be used for tracking the console focus. Basically, the result of filtering out
     * ANSI control codes.
     */
    private void handleUserInput (char c) {
        // Send data to the userInputWriter
        userInputWriter.putChar(c, isReadingInput);
        
        // Notify the readLine thread that a character was pressed so it checks whether or not it can finish
        // This does nothing if no readLine is blocking
        synchronized (charWaiter) {
            charWaiter.notifyAll();
        }
    }
    
    /**
     * The runnable underlying a constantly-running thread which accepts input from the console,
     * updating the focus tracker with the latest data.
     */
    private void inputReaderRunnable () {
        try {
            int charIn;
            while ((charIn = reader.read()) != NonBlockingReader.EOF) {
                char ch = (char)charIn;
                
                // Space and enter characters should not be received through the terminal
                // but are instead received through the DriverStationDisableKeysHook
                
                // Note that on Windows, enter is generally represented as \r\n (including in command prompt
                // standard input), so both characters should be blocked. If \r is let through it would put
                // the cursor back at the beginning of the line
                if (ch != '\n' && ch != '\r' && ch != ' ')
                    focusTracker.receiveInput(ch);
            }
        } catch (IOException e) { }
    }
    
    /**
     * Reads a line from the terminal.
     * @return The line, including the final {@code \n} newline character.
     */
    public synchronized String readLine () {
        
        // Write any text waiting from before
        userInputWriter.writeBufferContents();
        
        // Set isReadingInput to true so new input is read
        isReadingInput = true;
        
        // Keep waiting for character presses until a full line is read
        while (!userInputWriter.hasFullLine()) {
            try {
                synchronized (charWaiter) {
                    charWaiter.wait();
                }
            } catch (InterruptedException e) { }
        }
        
        // After a full line has been read, set isReadingInput to false and return the line
        isReadingInput = false;
        return userInputWriter.getLine();
        
    }
    
    /**
     * Check whether or not there is user input ready.
     */
    public boolean ready () {
        return userInputWriter.ready();
    }
    
    /**
     * Clear any waiting user input.
     */
    public void clearBuffer () {
        userInputWriter.clearBuffer();
    }
    
}
