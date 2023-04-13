package claw.subsystems;

import claw.rct.base.console.ConsoleManager;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestContext {
    
    public final ConsoleManager console;
    private boolean terminated = false;
    
    public SubsystemTestContext (ConsoleManager console) {
        this.console = console;
    }
    
    private void useContext () throws TestEndedException {
        if (terminated) throw new TestEndedException();
    }
    
    public void terminate () {
        terminated = true;
    }
    
    public static class TestEndedException extends Exception {
        public TestEndedException () {
            super("The subsystem test has been terminated.");
        }
    }
    
}
