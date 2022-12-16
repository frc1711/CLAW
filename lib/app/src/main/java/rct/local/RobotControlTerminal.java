package rct.local;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import rct.commands.Command.ParseException;
import rct.local.LocalSystem.NoResponseException;

public class RobotControlTerminal {
    
    private static final double CONNECTION_WATCHER_REFRESH_TIME = 3;
    private static final int
        TEAM_NUMBER = 1711,
        REMOTE_PORT = 5800;
    
    private final ConsoleManager console;
    private final Scanner scanner;
    private LocalSystem system;
    private boolean requireNewConnection = false;
    
    public RobotControlTerminal (ConsoleManager console) {
        this.console = console;
        this.scanner = new Scanner(System.in);
    }
    
    public void start () {
        getLocalSystem();
        
        console.print("\n");
        
        if (system == null) System.exit(1);
        
        startConnectionWatcherThread();
        
        while (true) {
            console.printSys("> ");
            console.flush();
            
            try {
                processCommand(scanner.nextLine());
            } catch (NoSuchElementException e) {
                break;
            }
            
            console.print("\n");
        }
    }
    
    private void startConnectionWatcherThread () {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep((long)(CONNECTION_WATCHER_REFRESH_TIME*1000));
                } catch (InterruptedException e) { }
                
                try {
                    if (requireNewConnection) system.establishNewConnection(TEAM_NUMBER, REMOTE_PORT);
                    requireNewConnection = false;
                } catch (IOException e) { }
            }
        });
        
        thread.start();
    }
    
    private void socketReconnect () {
        requireNewConnection = true;
    }
    
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
    
    private boolean getYesOrNo (String prompt) {
        String res = "";
        while (true) {
            console.printSys(prompt+" (y|n) ");
            console.flush();
            
            res = scanner.nextLine().strip().toLowerCase();
            if (res.equals("n")) return false;
            else if (res.equals("y")) return true;
        }
    }
    
    private void processCommand (String line) {
        try {
            system.processCommand(line);
        } catch (ParseException e) {
            console.printlnErr("Malformatted command: " + e.getMessage());
        } catch (NoResponseException e) {
            // TODO: Local command like "error [error type]" can further explain error messages like this
            console.printlnErr("Timeout reached: No response was received for the last command sent to remote.");
        } catch (IOException e) {
            console.printlnErr("The command failed to send to remote.");
            socketReconnect();
        }
    }
    
}
