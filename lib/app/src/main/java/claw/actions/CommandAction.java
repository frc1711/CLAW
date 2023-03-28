package claw.actions;

import java.util.concurrent.locks.ReentrantLock;

import claw.rct.network.low.concurrency.Waiter;
import claw.rct.network.low.concurrency.Waiter.NoValueReceivedException;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.WrapperCommand;

class CommandAction implements Action {
    
    /**
     * If the command scheduler takes longer than this amount of time to schedule a command,
     * then a runtime exception will be thrown for the action saying that the command failed
     * to schedule.
     */
    private static final long MILLIS_TO_SCHEDULE_COMMAND = 50;
    
    private final Command command;
    private final ReentrantLock runningLock = new ReentrantLock();
    
    public CommandAction (Command command) {
        this.command = command;
    }
    
    @Override
    public void run () {
        
        if (!runningLock.tryLock()) {
            throw new RuntimeException("Cannot run a command action more than one time at once");
        }
        
        try {
            if (CommandScheduler.getInstance().isScheduled(command)) {
                throw new RuntimeException("Cannot schedule this action's underlying command because it is already schedule");
            }
            
            // Get a new command adapter
            CommandAdapter adapter = new CommandAdapter();
            
            // TODO: Schedule in a thread-safe way
            CommandScheduler.getInstance().schedule(adapter);
            
            // Wait for the command to be scheduled
            adapter.waitForInitialize();
            
            // Wait for the command to finish
            adapter.waitForEnd();
            
        } finally {
            runningLock.unlock();
        }
        
    }
    
    private class CommandAdapter extends WrapperCommand {
        
        // Each command adapter will only be used once, so we can use fields like this to describe
        // whether the command was initialized, finished, etc.
        private boolean hasInitialized = false;
        private final Waiter<Object> initializeWaiter = new Waiter<>();
        
        private boolean hasEnded = false;
        private final Waiter<Object> endWaiter = new Waiter<>();
        
        public CommandAdapter () {
            super(command);
        }
        
        @Override
        public void initialize () {
            hasInitialized = true;
            initializeWaiter.receive(new Object());
            super.initialize();
        }
        
        @Override
        public void end (boolean interrupted) {
            hasEnded = true;
            endWaiter.receive(new Object());
            super.end(interrupted);
        }
        
        public void waitForInitialize () {
            if (!hasInitialized) {
                try {
                    initializeWaiter.waitForValue(MILLIS_TO_SCHEDULE_COMMAND);
                } catch (NoValueReceivedException e) {
                    throw new RuntimeException("The CommandScheduler failed to schedule the command");
                }
            }
        }
        
        public void waitForEnd () {
            if (!hasEnded) {
                try {
                    endWaiter.waitForValue();
                } catch (NoValueReceivedException e) {
                    throw new RuntimeException("Oh no! You hopefully should never see this (endWaiter was killed)");
                }
            }
        }
        
    }
    
    
    
}
