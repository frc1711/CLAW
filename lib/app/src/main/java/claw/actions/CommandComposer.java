package claw.actions;

import edu.wpi.first.wpilibj2.command.Command;

public class CommandComposer {
    
    private CommandComposer () { }
    
    public void run (Command command) {
        new CommandExecutor(command).execute();
    }
    
    public <T> T runGet (FunctionalCommandBase<T> command) {
        run(command);
        return command.getValue();
    }
    
    public void delay (double durationSecs) {
        long targetTime = System.currentTimeMillis() + (long)(1000 * durationSecs);
        long timeToWait = targetTime - System.currentTimeMillis();
        
        while (timeToWait > 0) {
            try {
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) { }
            
            timeToWait = targetTime - System.currentTimeMillis();
        }
    }
    
}
