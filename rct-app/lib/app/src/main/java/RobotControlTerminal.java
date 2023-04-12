package claw.rct.local;

import java.io.IOException;

import claw.rct.commands.RCTCommand.ParseException;
import claw.actions.compositions.Context.TerminatedContextException;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.local.LocalSystem.ConnectionStatus;
import claw.rct.local.console.LocalConsoleManager;
import claw.rct.console.ConsoleManager;

/**
 * Represents the main robot control terminal program which starts the driverstation side of the server
 * and controls input into the {@link LocalSystem} to be processed as commands.
 */
public class RobotControlTerminal {
    
    private static final int
        TEAM_NUMBER = 1711,
        REMOTE_PORT = 5800;
    
    private final ConsoleManager console;
    
    /**
     * Creates a new robot control terminal.
     */
    public RobotControlTerminal () throws IOException {
        this.console = new LocalConsoleManager();
    }
    
    /**
     * Starts the robot control terminal.
     */
    public void start () throws TerminatedContextException {
        
        // LocalSystem setup
        console.printlnSys("Attempting to connect to roboRIO server...");
        LocalSystem system = new LocalSystem(
            TEAM_NUMBER,
            REMOTE_PORT,
            new LogDataStorage(),
            console
        );
        
        // Display comms status to the user
        ConnectionStatus comms = system.checkServerConnection();
        if (comms == ConnectionStatus.OK) {
            console.printlnSys("Connected to Robot Control Terminal server successfully.");
        } else {
            console.printlnErr("Warning: Connection status " + comms.name() + " - Use 'comms' for details.");
        }
        
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
    
    private void processCommand (LocalSystem system, String line) throws TerminatedContextException {
        try {
            system.processCommand(line);
        } catch (ParseException e) {
            console.printlnErr(e.getMessage());
        } catch (BadCallException e) {
            console.printlnErr(e.getMessage());
        } catch (IOException e) {
            console.printlnErr("The connection with remote failed.");
        }
    }
    
}
