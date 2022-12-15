package rct.local;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import rct.commands.Command.ParseException;
import rct.local.LocalSystem.NoResponseException;

public class RobotControlTerminal {
    
    private final ConsoleManager console;
    private final Scanner scanner;
    
    public RobotControlTerminal (ConsoleManager console) {
        this.console = console;
        this.scanner = new Scanner(System.in);
    }
    
    public void start () {
        LocalSystem system = getLocalSystem();
        console.print("\n");
        
        if (system == null) System.exit(1);
        
        while (true) {
            console.printSys("\n> ");
            console.flush();
            
            try {
                processCommand(system, scanner.nextLine());
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }
    
    private void handleSocketException (IOException e) {
        console.printlnErr("\n\nA fatal socket exception occurred (connection to the robot was broken).\n");
        e.printStackTrace();
        System.exit(1);
    }
    
    private LocalSystem getLocalSystem () {
        LocalSystem system = null;
        
        while (system == null) {
            try {
                system = new LocalSystem(1711, 5800, 5, new StreamDataStorage(), console, this::handleSocketException);
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
    
    private void processCommand (LocalSystem system, String line) {
        try {
            system.processCommand(line);
        } catch (ParseException e) {
            console.printlnErr("Malformatted command: " + e.getMessage());
        } catch (NoResponseException e) {
            console.printlnErr("Timeout reached: No response was received for the last command sent to remote.");
        } catch (IOException e) {
            console.printlnErr("The command failed to send to remote.");
        }
    }
    
}
