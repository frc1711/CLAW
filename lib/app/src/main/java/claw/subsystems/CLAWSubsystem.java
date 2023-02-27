package claw.subsystems;

import java.util.HashMap;
import java.util.Set;

import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class CLAWSubsystem extends SubsystemBase {
    
    private static final HashMap<String, CLAWSubsystem> subsystems = new HashMap<>();
    
    public static final CommandProcessor COMMAND_PROCESSOR = new CommandProcessor(
        "subsystem",
        "subsystem",
        "subsystem",
        CLAWSubsystem::subsystemCommand
    );
    
    private final HashMap<String, SubsystemTest> subsystemTests = new HashMap<>();
    
    public CLAWSubsystem () {
        synchronized (subsystems) {
            // Both necessary for the subsystems map and for helping protect users from multiple subsystem instantiations
            if (subsystems.containsKey(getName()))
                throw new IllegalArgumentException("Subsystem with name '"+getName()+"' cannot be instantiated more than once.");
            subsystems.put(getName(), this);
        }
    }
    
    private static void subsystemCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNoOptions();
        reader.allowNoFlags();
        String operation = reader.readArgOneOf("operation", "Expected 'list', 'inspect'.", "list", "inspect");
        
        if (operation.equals("list")) {
            reader.noMoreArgs();
            
            synchronized (subsystems) {
                subsystems.keySet().forEach(console::println);
                if (subsystems.size() == 0)
                    console.println("There are no instantiated CLAWSubsystems.");
            }
            
        } else if (operation.equals("inspect")) {
            
            synchronized (subsystems) {
                // Get the subsystem name
                String subsystemName = reader.readArgOneOf("subsystem name", "Expected the name of an exsiting subsystem.", subsystems.keySet());
                reader.noMoreArgs();
                
                // Get the subsystem and run inspect
                CLAWSubsystem subsystem = subsystems.get(subsystemName);
                subsystemInspect(console, subsystem);
            }
            
        }
        
    }
    
    private static void subsystemInspect (ConsoleManager console, CLAWSubsystem subsystem) {
        
        synchronized (subsystem.subsystemTests) {
            
            Set<String> testNames = subsystem.subsystemTests.keySet();
            if (testNames.size() == 0) {
                console.println("Subsystem has no tests.");
            } else {
                console.println("Subsystem tests:");
                testNames.forEach(console::println);
            }
            
        }
        
        console.println("Use 'test' to run a test with a given name, or 'quit' to stop inspecting the subsystem.");
        
        String lastInput = "";
        
        while (!lastInput.toLowerCase().equals("quit")) {
            
            lastInput = console.readInputLine().strip();
            console.println("(Implement the command interpreter for subsystem inspection)");
            
        }
        
    }
    
    protected void addTests (SubsystemTest... tests) {
        synchronized (subsystemTests) {
            for (SubsystemTest test : tests)
                subsystemTests.put(test.getName(), test);
        }
    }
    
    /**
     * Stop all subsystem actuation and put the subsystem into a safe state.
     */
    public abstract void stop ();
    
}
