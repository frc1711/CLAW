package claw.actions;

import java.util.Optional;
import java.util.function.Function;

import claw.LiveValues;
import claw.rct.base.console.ConsoleManager;
import claw.rct.base.console.ConsoleManager.TerminalKilledException;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestCompositionContext {
    
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
            throw new TerminatedContextException();
        }
        
        runAction(Action.delay(durationSecs));
    }
    
    public void run (Command command) throws TerminatedContextException {
        runAction(Action.fromCommand(command));
    }
    
    private void runAction (Action action) throws TerminatedContextException {
        try {
            console.flush();
        } catch (TerminalKilledException e) {
            terminate();
            throw new TerminatedContextException();
        }
        
        synchronized (lastActionLock) {
            lastAction = Optional.of(action);
        }
        
        useContext();
        action.run();
        useContext();
    }
    
    public void terminate () {
        terminated = true;
        synchronized (lastActionLock) {
            lastAction.ifPresent(Action::cancel);
        }
    }
    
    public boolean isTerminated () {
        return terminated;
    }
    
    public SubsystemTestCompositionContext (ConsoleManager console, CLAWSubsystem subsystem) {
        this.console = console;
        this.subsystem = subsystem;
    }
    
    public void useContext () throws TerminatedContextException {
        if (isTerminated()) {
            throw new TerminatedContextException();
        }
        
        try {
            console.useContext();
        } catch (TerminalKilledException e) {
            terminate();
            throw new TerminatedContextException();
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
    
    public static class TerminatedContextException extends Exception {
        public TerminatedContextException () {
            super("The context in which this operation has been performed was terminated.");
        }
    }
    
    public static interface SubsystemTestOperation {
        public void runOnContext (SubsystemTestCompositionContext context) throws TerminatedContextException, TerminalKilledException;
    }
    
    public static Action compose (
        ConsoleManager console,
        CLAWSubsystem subsystem,
        SubsystemTestOperation operation
    ) {
        return new Action() {
            
            private Optional<SubsystemTestCompositionContext> context = Optional.empty();
            
            @Override
            public void runAction () {
                // Fill the context field
                SubsystemTestCompositionContext ctx = new SubsystemTestCompositionContext(console, subsystem);
                context = Optional.of(ctx);
                
                // Try to execute the operation, ignoring termination exceptions
                try {
                    operation.runOnContext(ctx);
                } catch (TerminatedContextException e) {
                } catch (TerminalKilledException e) { }
                
            }
            
            @Override
            public void cancelRunningAction () {
                context.ifPresent(SubsystemTestCompositionContext::terminate);
            }
            
        };
    }
    
}
