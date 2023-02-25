package claw.testing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;

import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;

public class LiveCommandTester {
    
    private final String description;
    private final Consumer<LiveValues> periodicExecute;
    private final Runnable putInSafeState;
    private final Subsystem[] subsystems;
    
    public LiveCommandTester (String description, Consumer<LiveValues> periodicExecute, Runnable putInSafeState, Subsystem... subsystems) {
        this.description = description;
        this.periodicExecute = periodicExecute;
        this.putInSafeState = putInSafeState;
        this.subsystems = subsystems;
    }
    
    public CommandProcessor toCommandProcessor (String commandName) {
        return new CommandProcessor(
            commandName,
            commandName,
            "Use this command to run a custom test command on the robot.",
            this::runCommand
        );
    }
    
    public class TestCommand extends CommandBase {
        
        private final LiveValues values;
        
        public TestCommand (LiveValues values) {
            this.values = values;
            addRequirements(subsystems);
        }
        
        @Override
        public void initialize () {
            putInSafeState.run();
        }
        
        @Override
        public void execute () {
            periodicExecute.accept(values);
        }
        
        @Override
        public void end (boolean interrupted) {
            putInSafeState.run();
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
    
    private void runCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
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
        LiveValues values = new LiveValues();
        TestCommand command = new TestCommand(values);
        command.withInterruptBehavior(InterruptionBehavior.kCancelIncoming).schedule();
        console.printlnSys("\nRunning test command");
        
        while (DriverStation.isEnabled()) {
            values.update(console);
            console.flush();
        }
        
        // Stop the command
        console.printlnSys("\nStopping command");
        command.cancel();
        
    }
    
    public static class LiveValues {
        
        private final Object fieldsLock = new Object();
        private final ArrayList<String> fields = new ArrayList<>();
        private final ArrayList<String> values = new ArrayList<>();
        private final HashSet<String> newFieldNames = new HashSet<>();
        private final HashSet<String> updatedFields = new HashSet<>();
        
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
        
        public void setField (String fieldName, double value) {
            setField(fieldName, Double.toString(value));
        }
        
        public void setField (String fieldName, int value) {
            setField(fieldName, Integer.toString(value));
        }
        
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
        
        private static void printField (ConsoleManager console, String fieldName, String value) {
            String space = " ".repeat(Math.max(0, 18 - fieldName.length()));
            console.println(fieldName + " : " + space + value);
        }
        
    }
    
}
