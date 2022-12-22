package rct.local;

import java.io.IOException;

import rct.commands.Command.ParseException;
import rct.commands.CommandInterpreter.BadArgumentsException;
import rct.local.LocalSystem.NoResponseException;

public class RobotControlTerminal {
    
    private static final double CONNECTION_WATCHER_REFRESH_TIME = 3;
    private static final int
        TEAM_NUMBER = 1711,
        REMOTE_PORT = 5800;
    
    private final ConsoleManager console;
    private LocalSystem system;
    private boolean requireNewConnection = false;
    
    public RobotControlTerminal (ConsoleManager console) {
        this.console = console;
    }
    
    public void start () {
        
        // LocalSystem setup
        getLocalSystem();
        console.print("\n");
        if (system == null) System.exit(1);
        
        // Start a thread watching the connection to the robot, restoring it when necessary
        startConnectionWatcherThread();
        
        while (true) {
            // Clear any waiting input lines
            console.clearWaitingInputLines();
            
            // Display the prompt
            console.printSys("> ");
            
            // Process the next input line
            String inputLine = console.readInputLine();
            if (inputLine.isBlank()) {
                console.println("");
                continue;
            }
            
            processCommand(inputLine);
            
            // Add an extra print statement before processing the next command
            console.print("\n");
        }
        
    }
    
    /**
     * Watch the connection to the robot indefinitely in a separate thread, restoring it when necessary
     */
    private void startConnectionWatcherThread () {
        // Watch the connection to the robot indefinitely in a separate thread,
        // restoring it when necessary
        Thread thread = new Thread(() -> {
            while (true) {
                // Wait for a time before trying again
                try {
                    Thread.sleep((long)(CONNECTION_WATCHER_REFRESH_TIME*1000));
                } catch (InterruptedException e) { }
                
                try {
                    // Check if a new connection to the server is required
                    if (requireNewConnection) system.establishNewConnection(REMOTE_PORT);
                    
                    // If no exception has occurred thus far, a new connection was successfully created
                    // and the requireNewConnection flag can be disabled
                    requireNewConnection = false;
                } catch (IOException e) { }
            }
        });
        
        thread.start();
    }
    
    /**
     * Set a flag so that the connection watcher thread will reconnect to the robot
     */
    private void socketReconnect () {
        requireNewConnection = true;
    }
    
    /**
     * Get a new local system (for the first time) using basic interactive UI if problems occur. It sets {@code this.system}
     * to the retrieved {@link LocalSystem}. {@code this.system} can also be set to {@code null} if this method fails.
     */
    private LocalSystem getLocalSystem () {
        system = null;
        
        while (system == null) {
            try {
                system = new LocalSystem(TEAM_NUMBER, REMOTE_PORT, 5, new StreamDataStorage(), console, e -> socketReconnect());
            } catch (IOException e) {
                if (!getYesOrNo("\nTry again?")) return null;
                console.clear();
            }
        }
        
        return system;
    }
    
    /**
     * Ask a yes or no question through the console manager, returning {@code true} if {@code y} is inputted
     * and {@code false} if {@code n} is inputted.
     */
    private boolean getYesOrNo (String prompt) {
        String res = "";
        while (true) {
            console.printSys(prompt+" (y|n) ");
            console.flush();
            
            res = console.readInputLine().strip().toLowerCase();
            if (res.equals("n")) return false;
            else if (res.equals("y")) return true;
        }
    }
    
    private void processCommand (String line) {
        try {
            system.processCommand(line);
        } catch (ParseException e) {
            console.printlnErr("Malformatted command: " + e.getMessage());
        } catch (BadArgumentsException e) {
            console.printlnErr(e.getMessage());
        } catch (NoResponseException e) {
            // TODO: Local command like "error [error type]" can further explain error messages like this
            console.printlnErr("Timeout reached: No response was received for the last command sent to remote.");
        } catch (IOException e) {
            console.printlnErr("The command failed to send to remote.");
            socketReconnect();
        }
    }
    
}
