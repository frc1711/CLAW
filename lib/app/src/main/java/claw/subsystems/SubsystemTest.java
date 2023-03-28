package claw.subsystems;

import java.util.Optional;

import claw.LiveValues;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * Represents a simple test bound to a subsystem which can be run through the Robot Control Terminal.
 * @see CLAWSubsystem#addTests(SubsystemTest...)
 */
public class SubsystemTest {
    
    private final String name, description;
    private final TestSection[] testSections;
    
    /**
     * Create a new {@link SubsystemTest}. A subsystem test should only ever control one subsystem to which it belongs.
     * Never add a subsystem test to a subsystem which it does not belong to.
     * @param name              A name to identify the particular test by.
     * @param description       A description of what the test does and how it can be used.
     * @param periodicExecute   A {@link LiveValues} consumer which will be run periodically (generally around once every 20ms)
     * to control the subsystem and perform the test's operations. The {@code LiveValues} can be used to display telemetry fields
     * in the console as the test runs.
     */
    public SubsystemTest (String name, String description, TestSection... testSections) {
        this.name = name;
        this.description = description;
        this.testSections = testSections.clone();
    }
    
    /**
     * Get the {@link SubsystemTest}'s name.
     * @return  The name of this test.
     */
    public String getName () {
        return name;
    }
    
    private static boolean getYesNo (ConsoleManager console, String prompt) {
        Optional<Boolean> answer = Optional.empty();
        
        console.println("");
        
        while (answer.isEmpty()) {
            console.moveUp(1);
            console.clearLine();
            console.print(prompt + "(yes | no) ");
            
            String input = console.readInputLine().strip().toUpperCase();
            
            answer = input.equals("YES") ? Optional.of(true)
                : (input.equals("NO") ? Optional.of(false)
                : Optional.empty());
        }
        
        return answer.get();
    }
    
    /**
     * Run the subsystem command through a given console.
     * @param console
     */
    void run (ConsoleManager console, CLAWSubsystem subsystem) {
        
        // Display description and an important safety warning
        console.println("Double-tap enter to disable the robot and stop the test command at any time.");
        
        console.println("Usage and description:");
        console.println(ConsoleManager.formatMessage(description, 2));
        
        // Wait until input after the driverstation is enabled
        boolean runCommand = getYesNo(console, "Run the command? ");
        if (!runCommand) return;
        
        // TODO: Checking for robot enable for individual test commands
        while (DriverStation.isDisabled()) {
            console.printlnErr("Enable the robot and try again.");
            runCommand = getYesNo(console, "Run the command? ");
            if (!runCommand) return;
        }
        
        // Run each test section, one by one
        console.printlnSys("\nRunning test command");
        for (TestSection section : testSections) {
            section.run(subsystem, getName(), console);
        }
        
        // TODO: Implement code to disable all the test sections on robot disable
        
        // Indicate that the test is finished
        console.printlnSys("\nTest concluded");
        
    }
    
    public static interface TestSection {
        
        /**
         * This should run the entirety of the test section (blocking).
         * @param subsystem The {@link CLAWSubsystem} this test section is linked to.
         * @param testName  The name of this subsystem test.
         * @param console   The {@link ConsoleManager} this test section will be linked to.
         */
        public void run (CLAWSubsystem subsystem, String testName, ConsoleManager console);
        
    }
    
}
