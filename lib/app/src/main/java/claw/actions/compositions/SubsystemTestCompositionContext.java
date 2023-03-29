package claw.actions.compositions;

import claw.rct.network.low.ConsoleManager;
import claw.subsystems.CLAWSubsystem;

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
    
}
