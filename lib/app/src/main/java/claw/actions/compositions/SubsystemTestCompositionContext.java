package claw.actions.compositions;

import java.util.function.Function;

import claw.LiveValues;
import claw.actions.Action;
import claw.actions.ParallelAction;
import claw.rct.network.low.ConsoleManager;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestCompositionContext extends CommandCompositionContext {
    
    public final ConsoleManager console;
    public final CLAWSubsystem subsystem;
    
    public SubsystemTestCompositionContext (ConsoleManager console, CLAWSubsystem subsystem) {
        this.console = console;
        this.subsystem = subsystem;
    }
    
    @Override
    public void onTerminate () {
        console.terminate();
    }
    
    @Override
    public void useContext () {
        super.useContext();
        console.useContext();
    }
    
    public void withLiveValues (Function<LiveValues, Command> commandSupplier) {
        LiveValues debugValues = new LiveValues();
        Command command = commandSupplier.apply(debugValues);
        
        runAction(new ParallelAction(
            Action.fromCommand(command),
            new Action() {
                
                private boolean running = false;
                
                @Override
                public void runAction () {
                    running = true;
                    while (running) {
                        // TODO: console can throw an error when used like this, which would go uncaught. Consider how to
                        // properly encapsulate the ActionCompositionContext
                        debugValues.update(console);
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
