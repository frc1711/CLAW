package rct.local;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

public abstract class ConsoleManager {
    
    private final BufferedInputStream inputStream;
    
    public ConsoleManager () {
        inputStream = new BufferedInputStream(System.in);
    }
    
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
    
    public boolean hasInputReady () {
        try {
            return inputStream.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }
    
    public abstract void moveUp (int lines);
    public abstract void clearLine ();
    
    public abstract void saveCursorPos ();
    public abstract void restoreCursorPos ();
    
    public abstract void print (String msg);
    public void println (String msg) {
        print(msg + "\n");
    }
    
    public abstract void printErr (String msg);
    public void printlnErr (String msg) {
        printErr(msg + "\n");
    }
    
    public abstract void printSys (String msg);
    public void printlnSys (String msg) {
        printSys(msg + "\n");
    }
    
    public void flush () {
        System.out.flush();
    }
    
    public abstract void clear ();
    
}
