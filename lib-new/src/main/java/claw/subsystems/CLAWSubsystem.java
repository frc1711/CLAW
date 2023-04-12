package claw.subsystems;

import java.util.HashMap;
import java.util.Set;

import claw.actions.compositions.Context;
import claw.actions.compositions.Context.TerminatedContextException;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.console.ConsoleManager;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class CLAWSubsystem extends SubsystemBase {
    
    private static final HashMap<String, CLAWSubsystem> subsystems = new HashMap<>();
    
    public static final CommandProcessor COMMAND_PROCESSOR = new CommandProcessor(
        "subsystem",
        "subsystem [ list | inspect | test]",
        "Use 'subsystem list' to list all subsystems. 'subsystem inspect NAME' will retrieve the available tests for a given " +
        "subsystem. 'subsystem test SUBNAME TESTNAME' will run a test named TESTNAME on the given subsystem SUBNAME.",
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
    
    private static void subsystemCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
        reader.allowNoOptions();
        reader.allowNoFlags();
        String operation = reader.readArgOneOf("operation", "Expected 'list', 'inspect' or 'test'.", "list", "inspect", "test");
        
        if (operation.equals("list")) {
            
            // List all the instantiated subsystems
            
            reader.noMoreArgs();
            
            synchronized (subsystems) {
                subsystems.keySet().forEach(str -> Context.ignoreTermination(() -> console.println(str)));
                if (subsystems.size() == 0)
                    console.println("There are no instantiated CLAWSubsystems.");
            }
            
        } else {
            
            // Operations which are performed on a specific subsystem
            
            // Get the CLAWSubsystem
            CLAWSubsystem subsystem;
            synchronized (subsystems) {
                // Get the subsystem name
                String subsystemName = reader.readArgOneOf("subsystem name", "Expected the name of an exsiting subsystem.", subsystems.keySet());
                
                // Get the subsystem and print its supported tests
                subsystem = subsystems.get(subsystemName);
            }
            
            // Switch based on operation
            if (operation.equals("inspect")) {
                
                reader.noMoreArgs();
                
                // Show available tests for the subsystem
                if (subsystem.subsystemTests.size() == 0) {
                    console.println(subsystem.getName()+" supports no tests.");
                } else {
                    console.println(subsystem.getName()+" supports the following tests:");
                    synchronized (subsystem.subsystemTests) {
                        subsystem.subsystemTests.keySet().forEach(str -> Context.ignoreTermination(() -> console.println(str)));
                    }
                }
                
            } else if (operation.equals("test")) {
                
                // Run a subsystem test
                
                // Get the test specified by the command
                SubsystemTest test;
                synchronized (subsystem.subsystemTests) {
                    // Get a test name to run
                    Set<String> testNames = subsystem.subsystemTests.keySet();
                    String testName = reader.readArgOneOf(
                        "test name",
                        "Expected a test name from the set of tests supported by the subsystem.",
                        testNames
                    );
                    reader.noMoreArgs();
                    
                    test = subsystem.subsystemTests.get(testName);
                }
                
                // Run the subsystem test
                test.run(console, subsystem);
                
            }
            
        }
        
    }
    
    /**
     * Bind some {@link SubsystemTest}s to this subsystem so they can be easily run through the Robot Control Terminal.
     * Make sure that only this subsystem is every controlled by these tests. 
     * @param tests The {@code SubsystemTest}s to bind to this subsystem.
     */
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
