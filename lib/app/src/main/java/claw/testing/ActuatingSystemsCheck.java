package claw.testing;

import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;

public abstract class ActuatingSystemsCheck implements SystemsCheck {
    
    public final boolean run (ConsoleManager console) {
        // Print the header info
        String subsystemNames = getActuatedSubsystemNames();
        console.println("Warning: These systems are actuated with this systems check:" + subsystemNames);
        console.println("Double-tap the enter/return key at any time to disable the robot.");
        
        // Print another message if the robot is disabled
        if (DriverStation.isDisabled()) {
            console.printlnErr("The robot is currently disabled, and must be enabled before you can continue.");
        }
        
        // Confirm that the user wants to begin the systems check
        ConfirmActuationInput input = ConfirmActuationInput.NONE;
        while (input == ConfirmActuationInput.NONE) {
            input = getConfirmActuationInput(console);
        }
        
        if (input == ConfirmActuationInput.CANCEL) {
            console.println("Canceling.");
            return false;
        }
        
        // Run the actuating systems check
        stopSystems();
        getCheckCommand().schedule();
        return runActuatingCheck(console);
    }
    
    private static ConfirmActuationInput getConfirmActuationInput (ConsoleManager console) {
        try {
            console.print("Type 'run' to confirm running the systems check, or 'cancel' to cancel: ");
            return ConfirmActuationInput.valueOf(console.readInputLine().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ConfirmActuationInput.NONE;
        }
    }
    
    private enum ConfirmActuationInput {
        RUN,
        CANCEL,
        NONE;
    }
    
    /**
     * Get the human-readable names of any subsystems actuated by this systems check.
     * @return
     */
    protected abstract String getActuatedSubsystemNames ();
    
    /**
     * Stop any actuators, putting the robot into a safe state to interact with.
     */
    protected abstract void stopSystems ();
    
    /**
     * Run the systems check. The command from {@link #getCheckCommand()} will be scheduled right before
     * this method is called.
     * @return Whether or not the systems check was successful.
     */
    protected abstract boolean runActuatingCheck (ConsoleManager console);
    
    /**
     * Get a command to actuate any systems. Do not use console control within this command.
     * @return
     */
    protected abstract Command getCheckCommand ();
    
}
