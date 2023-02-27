package claw.subsystems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;

/**
 * Represents a simple test bound to a subsystem which can be run through the Robot Control Terminal.
 * @see CLAWSubsystem#addTests(SubsystemTest...)
 */
public class SubsystemTest {
    
    private final String name, description;
    private final Consumer<LiveValues> periodicExecute;
    
    /**
     * Create a new {@link SubsystemTest}. A subsystem test should only ever control one subsystem to which it belongs.
     * Never add a subsystem test to a subsystem which it does not belong to.
     * @param name              A name to identify the particular test by.
     * @param description       A description of what the test does and how it can be used.
     * @param periodicExecute   A {@link LiveValues} consumer which will be run periodically (generally around once every 20ms)
     * to control the subsystem and perform the test's operations. The {@code LiveValues} can be used to display telemetry fields
     * in the console as the test runs.
     */
    public SubsystemTest (String name, String description, Consumer<LiveValues> periodicExecute) {
        this.name = name;
        this.description = description;
        this.periodicExecute = periodicExecute;
    }
    
    /**
     * Get the {@link SubsystemTest}'s name.
     * @return  The name of this test.
     */
    public String getName () {
        return name;
    }
    
    private class SubsystemTestCommand implements Command {
        
        private final LiveValues values = new LiveValues();
        private final CLAWSubsystem subsystem;
        
        public SubsystemTestCommand (CLAWSubsystem subsystem) {
            this.subsystem = subsystem;
        }
        
        @Override
        public String getName () {
            return "SubsystemTestCommand<"+subsystem.getName()+">(\""+name+"\")";
        }
        
        @Override
        public Set<Subsystem> getRequirements () {
            HashSet<Subsystem> reqs = new HashSet<>();
            reqs.add(subsystem);
            return reqs;
        }
        
        @Override
        public void initialize () {
            subsystem.stop();
        }
        
        @Override
        public void execute () {
            periodicExecute.accept(values);
        }
        
        @Override
        public void end (boolean interrupted) {
            subsystem.stop();
        }
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
        
        while (DriverStation.isDisabled()) {
            console.printlnErr("Enable the robot and try again.");
            runCommand = getYesNo(console, "Run the command? ");
            if (!runCommand) return;
        }
        
        // Run the command
        SubsystemTestCommand command = new SubsystemTestCommand(subsystem);
        command.withInterruptBehavior(InterruptionBehavior.kCancelIncoming).schedule();
        console.printlnSys("\nRunning test command");
        
        while (DriverStation.isEnabled()) {
            command.values.update(console);
            console.flush();
        }
        
        // Stop the command
        console.printlnSys("\nStopping command");
        command.cancel();
        
    }
    
    /**
     * A class allowing for the live updating of fields to be displayed in the console. This is used for telemetry
     * in a subsystem test.
     */
    public static class LiveValues {
        
        /**
         * An object lock to be synchronized on before making any modifications to the fields,
         * values, etc.
         */
        private final Object fieldsLock = new Object();
        
        private final ArrayList<String> fields = new ArrayList<>();
        private final ArrayList<String> values = new ArrayList<>();
        private final HashSet<String> newFieldNames = new HashSet<>();
        private final HashSet<String> updatedFields = new HashSet<>();
        
        private LiveValues () { }
        
        /**
         * Set a live field to a string value to be displayed in the console.
         * @param fieldName The name of the field under which the value should be displayed.
         * @param value     The value to put to the console.
         */
        public void setField (String fieldName, String value) {
            synchronized (fieldsLock) {
                // Get the index of the field in the fields and values arrays
                int fieldIndex = fields.indexOf(fieldName);
                
                if (fieldIndex == -1) {
                    
                    // Add a new field and value
                    newFieldNames.add(fieldName);
                    fields.add(fieldName);
                    values.add(value);
                    
                } else if (!values.get(fieldIndex).equals(value)) {
                    
                    // Update the value only, but first check if it's the same as it was before
                    values.set(fieldIndex, value);
                    updatedFields.add(fieldName);
                    
                }
            }
        }
        
        /**
         * Set a live field to a double value to be displayed in the console.
         * @param fieldName The name of the field under which the value should be displayed.
         * @param value     The value to put to the console.
         */
        public void setField (String fieldName, double value) {
            setField(fieldName, Double.toString(value));
        }
        
        /**
         * Set a live field to an integer value to be displayed in the console.
         * @param fieldName The name of the field under which the value should be displayed.
         * @param value     The value to put to the console.
         */
        public void setField (String fieldName, int value) {
            setField(fieldName, Integer.toString(value));
        }
        
        /**
         * Update all the fields in the console to display the latest values.
         * @param console
         */
        private void update (ConsoleManager console) {
            synchronized (fieldsLock) {
                
                // Move up to the top of the preexisting lines
                int preexistingLines = fields.size() - newFieldNames.size();
                console.moveUp(preexistingLines);
                
                // Iterate through all existing fields
                for (int i = 0; i < fields.size(); i ++) {
                    String fieldName = fields.get(i);
                    String value = values.get(i);
                    
                    // Check for no-change vs. updated vs. new field
                    if (newFieldNames.contains(fieldName)) {
                        
                        // New field
                        printField(console, fieldName, value);
                        
                    } else if (updatedFields.contains(fieldName)) {
                        
                        // Updated field
                        console.clearLine();
                        printField(console, fieldName, value);
                        
                    } else {
                        
                        // No change to field
                        console.moveUp(-1);
                        
                    }
                    
                }
                
                // Clear updated fields and new fields
                newFieldNames.clear();
                updatedFields.clear();
                
            }
        }
        
        /**
         * Print a single field to the console
         */
        private static void printField (ConsoleManager console, String fieldName, String value) {
            String space = " ".repeat(Math.max(0, 18 - fieldName.length()));
            console.println(fieldName + " : " + space + value);
        }
        
    }
    
}
