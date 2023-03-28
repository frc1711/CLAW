package claw.actions;

import claw.CLAWRobot;
import claw.rct.network.low.concurrency.Waiter;
import claw.rct.network.low.concurrency.Waiter.NoValueReceivedException;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.WrapperCommand;

class CommandExecutorAction extends Action {
    
    /**
     * If the command scheduler takes longer than this amount of time to schedule a command,
     * then a runtime exception will be thrown for the action saying that the command failed
     * to schedule.
     */
    private static final long MILLIS_TO_SCHEDULE_COMMAND = 1200;
    
    private final Command command;
    
    public CommandExecutorAction (Command command) {
        this.command = command;
    }
    
    @Override
    protected void runAction () {
        
        // Get a new command adapter
        CommandAdapter adapter = new CommandAdapter();
        
        // Schedule the command in a thread-safe way because the CommandScheduler is not thread safe
        CLAWRobot.executeInMainRobotThread(() -> {
            CommandScheduler.getInstance().schedule(adapter);
        });
        
        // Wait for the command to be scheduled
        adapter.waitForInitialize();
        
        // Wait for the command to finish
        adapter.waitForEnd();
        
    }
    
    @Override
    protected void cancelRunningAction () {
        CLAWRobot.executeInMainRobotThread(() -> {
            CommandScheduler.getInstance().cancel(command);
        });
    }
    
    private class CommandAdapter extends WrapperCommand {
        
        // Each command adapter will only be used once, so we can use fields like this to describe
        // whether the command was initialized, finished, etc.
        private boolean hasInitialized = false;
        private final Waiter<Object> initializeWaiter = new Waiter<>();
        private boolean shouldCancel = false;
        
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
        
        @Override
        public boolean isFinished () {
            return shouldCancel || super.isFinished();
        }
        
        public void waitForInitialize () {
            if (!hasInitialized) {
                try {
                    initializeWaiter.waitForValue(MILLIS_TO_SCHEDULE_COMMAND);
                } catch (NoValueReceivedException e) {
                    shouldCancel = true;
                    throw new RuntimeException("The CommandScheduler failed to schedule the command before " + MILLIS_TO_SCHEDULE_COMMAND + "millisecond deadline");
                }
            }
        }
        
        public void waitForEnd () {
            if (!hasEnded) {
                try {
                    endWaiter.waitForValue();
                } catch (NoValueReceivedException e) {
                    throw new RuntimeException("Oh no! You hopefully should never see this (endWaiter was killed)");
                } finally {
                    shouldCancel = true;
                }
            }
        }
        
    }
    
    
    
}
