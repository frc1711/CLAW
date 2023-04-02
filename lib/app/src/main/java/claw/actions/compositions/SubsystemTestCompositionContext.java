package claw.actions.compositions;

import java.util.function.Function;

import claw.LiveValues;
import claw.actions.Action;
import claw.rct.network.low.ConsoleManager;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestCompositionContext<CTX extends SubsystemTestCompositionContext<CTX>>
    extends CommandCompositionContext<CTX> {
    
    public final ConsoleManager console;
    public final CLAWSubsystem subsystem;
    
    public SubsystemTestCompositionContext (ConsoleManager console, CLAWSubsystem subsystem) {
        this.console = console;
        this.subsystem = subsystem;
    }
    
    @Override
    public void useContext () throws TerminatedContextException {
        super.useContext();
        console.useContext();
    }
    
    public Command withLiveValues (Function<LiveValues, Command> commandSupplier) throws TerminatedContextException {
        
        // Get the command from the new LiveValues
        LiveValues debugValues = new LiveValues();
        Command command = commandSupplier.apply(debugValues);
        
        // Deadline the given command with an UpdateLiveValuesAction (turned into command) to automatically stop
        // updating live values when the given command finishes
        return command.deadlineWith(new UpdateLiveValuesAction(debugValues).toCommand());
        
    }
    
    private class UpdateLiveValuesAction extends Action {
                
        private boolean running = false;
        private final LiveValues values;
        
        public UpdateLiveValuesAction (LiveValues values) {
            this.values = values;
        }
        
        @Override
        public void runAction () {
            
            try {
                
                // Update indefinitely (until canceled)
                running = true;
                while (running) {
                    values.update(console);
                }
                
            } catch (TerminatedContextException e) {
                
                // If the console was terminated, then terminate the composition context
                terminate();
                
            }
            
        }
        
        @Override
        public void cancelRunningAction () {
            running = false;
        }
        
    }
    
}
