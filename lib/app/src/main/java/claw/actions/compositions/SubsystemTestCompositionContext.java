package claw.actions.compositions;

import claw.rct.network.low.ConsoleManager;

public class SubsystemTestCompositionContext extends CommandCompositionContext {
    
    public final ConsoleManager console;
    
    public SubsystemTestCompositionContext (ConsoleManager console) {
        this.console = console;
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
    
}
