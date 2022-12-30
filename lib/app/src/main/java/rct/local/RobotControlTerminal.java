package rct.local;

import java.io.IOException;

import rct.commands.Command.ParseException;
import rct.commands.CommandInterpreter.BadArgumentsException;
import rct.local.LocalSystem.NoResponseException;
import rct.network.low.ConsoleManager;

/**
 * Represents the main robot control terminal program which starts the driverstation side of the server
 * and controls input into the {@link LocalSystem} to be processed as commands.
 */
public class RobotControlTerminal {
    
    /**
     * The time interval between re-checking the connection status. Used by the connection watcher thread. 
     */
    private static final long CONNECTION_WATCHER_REFRESH_TIME_MILLIS = 3000;
    
    private static final int
        TEAM_NUMBER = 1711,
        REMOTE_PORT = 5800;
    
    private final ConsoleManager console;
    private boolean requireNewConnection = false;
    
    /**
     * Creates a new robot control terminal.
     */
    public RobotControlTerminal (ConsoleManager console) {
        this.console = console;
    }
    
    /**
     * Starts the robot control terminal.
     */
    public void start () {
        
        // LocalSystem setup
        LocalSystem system;
        try {
            system = getLocalSystem();
        } catch (IOException e) {
            console.println("");
            System.exit(1);
            return;
        }
        
        // Start a thread watching the connection to the robot, restoring it when necessary
        startConnectionWatcherThread(system);
        
        // Start the main command-line loop, and run it indefinitely
        console.println("");
        
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
            
            processCommand(system, inputLine);
            
            // Add an extra print statement before processing the next command
            console.print("\n");
        }
        
    }
    
    /**
     * Watch the connection to the robot indefinitely in a separate thread, restoring it when necessary.
     */
    private void startConnectionWatcherThread (LocalSystem system) {
        // Watch the connection to the robot indefinitely in a separate thread,
        // restoring it when necessary
        Thread thread = new Thread(() -> {
            while (true) {
                // Wait for a time before trying again
                try {
                    Thread.sleep(CONNECTION_WATCHER_REFRESH_TIME_MILLIS);
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
     * Get a new local system (for the first time) using basic UI if problems occur.
     * @return A new {@link LocalSystem} that has an open connection to remote.
     * @throws IOException If there were problems while connection, and the user indicated
     * that the app should not attempt to reconnect.
     */
    private LocalSystem getLocalSystem () throws IOException {
        LocalSystem system = null;
        
        while (system == null) {
            try {
                system = new LocalSystem(
                    TEAM_NUMBER,
                    REMOTE_PORT,
                    1,              // response timeout
                    0.2,            // keepalive send interval
                    new StreamDataStorage(),
                    console,
                    e -> socketReconnect());
            } catch (IOException e) {
                if (!getYesOrNo("\nTry again?"))
                    throw e;
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
    
    private void processCommand (LocalSystem system, String line) {
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
