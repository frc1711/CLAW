package claw.actions;

import java.util.concurrent.locks.ReentrantLock;

import edu.wpi.first.wpilibj2.command.Command;

public abstract class Action {
    
    public static Action fromCommand (Command command) {
        return new CommandExecutorAction(command);
    }
    
    public final Command toCommand () {
        return new CommandActionWrapper(this);
    }
    
    private final ReentrantLock runningLock = new ReentrantLock();
    
    public final void run () {
        if (!runningLock.tryLock()) {
            throw new RuntimeException("Action already running");
        }
        
        try {
            runAction();
        } finally {
            runningLock.unlock();
        }
    }
    
    public final void cancel () {
        if (isRunning()) {
            cancelRunningAction();
        }
    }
    
    public final boolean isRunning () {
        return runningLock.isLocked();
    }
    
    protected abstract void runAction ();
    protected abstract void cancelRunningAction ();
    
}
