package claw.actions;

import java.util.Set;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;

class CommandActionWrapper implements Command {
    
    private final Action action;
    
    public CommandActionWrapper (Action action) {
        this.action = action;
    }
    
    @Override
    public void initialize () {
        new Thread(action::run).start();
    }
    
    @Override
    public void end (boolean interrupted) {
        action.cancel();
    }
    
    @Override
    public boolean isFinished () {
        return !action.isRunning();
    }
    
    @Override
    public Set<Subsystem> getRequirements () {
        return Set.of();
    }
    
}
