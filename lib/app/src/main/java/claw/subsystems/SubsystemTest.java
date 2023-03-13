package claw.subsystems;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import claw.LiveValues;
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
    
}
