package claw.actions.compositions;

import java.util.Optional;
import java.util.function.Function;

import claw.LiveValues;
import claw.actions.Action;
import claw.actions.FunctionalCommand;
import claw.rct.base.console.ConsoleManager;
import claw.rct.base.console.ConsoleManager.TerminalKilledException;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestCompositionContext<CTX extends SubsystemTestCompositionContext<CTX>>
    implements Context<CTX> {
    
    public final ConsoleManager console;
    public final CLAWSubsystem subsystem;
    private boolean terminated = false;
    
    private Optional<Action> lastAction = Optional.empty();
    
    private final Object lastActionLock = new Object();
    
    public <T> T runGet (FunctionalCommand<T> command) throws TerminatedContextException {
        run(command);
        return command.getValue();
    }
    
    public void delay (double durationSecs) throws TerminatedContextException {
        try {
            console.flush();
        } catch (TerminalKilledException e) {
            terminate();
            throw getTerminatedException();
        }
        
        runAction(Action.delay(durationSecs));
    }
    
    public void run (Command command) throws TerminatedContextException {
        runAction(Action.fromCommand(command));
    }
    
    public void runAction (Action action) throws TerminatedContextException {
        try {
            console.flush();
        } catch (TerminalKilledException e) {
            terminate();
            throw getTerminatedException();
        }
        
        synchronized (lastActionLock) {
            lastAction = Optional.of(action);
        }
        
        useContext();
        action.run();
        useContext();
    }
    
    @Override
    public void terminate () {
        terminated = true;
        synchronized (lastActionLock) {
            lastAction.ifPresent(Action::cancel);
        }
    }
    
    @Override
    public boolean isTerminated () {
        return terminated;
    }
    
    public SubsystemTestCompositionContext (ConsoleManager console, CLAWSubsystem subsystem) {
        this.console = console;
        this.subsystem = subsystem;
    }
    
    @Override
    public void useContext () throws TerminatedContextException {
        Context.super.useContext();
        
        try {
            console.useContext();
        } catch (TerminalKilledException e) {
            terminate();
            throw getTerminatedException();
        }
    }
    
    public <T> T runLiveValuesGet (Function<LiveValues, FunctionalCommand<T>> commandSupplier) throws TerminatedContextException {
        LiveValues values = new LiveValues();
        FunctionalCommand<T> command = commandSupplier.apply(values);
        runLiveValuesCommand(values, command);
        return command.getValue();
    }
    
    public void runLiveValues (Function<LiveValues, Command> commandSupplier) throws TerminatedContextException {
        LiveValues values = new LiveValues();
        runLiveValuesCommand(values, commandSupplier.apply(values));
    }
    
    private void runLiveValuesCommand (LiveValues liveValues, Command command) throws TerminatedContextException {
        
        // Run the deadline
        runAction(Action.fromCommand(command).deadlineWith(new UpdateLiveValuesAction(liveValues)));
        
        // TODO: ALL WPILIB COMMAND CONSTRUCTORS CAN CALL METHODS ON THE COMMAND SCHEDULER LEADING TO CONCURRENTMODIFICATIONEXCEPTIONS
        
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
                
            } catch (TerminalKilledException e) {
                
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
