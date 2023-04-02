package claw.actions.compositions;

import java.util.function.Function;

import claw.LiveValues;
import claw.actions.Action;
import claw.actions.ParallelAction;
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
    
    public void withLiveValues (Function<LiveValues, Command> commandSupplier) throws TerminatedContextException {
        LiveValues debugValues = new LiveValues();
        Command command = commandSupplier.apply(debugValues);
        
        runAction(new ParallelAction(
            
            // Run the command
            Action.fromCommand(command),
            
            // Run an action which updates the LiveValues
            new Action() {
                
                private boolean running = false;
                
                @Override
                public void runAction () {
                    try {
                        running = true;
                        while (running) {
                            debugValues.update(console);
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
        ));
        
    }
    
}
