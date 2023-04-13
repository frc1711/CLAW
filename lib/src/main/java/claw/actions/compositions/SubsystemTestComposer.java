package claw.actions.compositions;

import claw.actions.compositions.SubsystemTestCompositionContext.Operation;
import claw.rct.base.console.ConsoleManager;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestComposer {
    
    public static Command compose (
        ConsoleManager console,
        CLAWSubsystem subsystem,
        Operation composition
    ) {
        return SubsystemTestCompositionContext.compose(
            () -> new SubsystemTestCompositionContext(console, subsystem),
            composition
        ).toCommand();
    }
    
    private SubsystemTestComposer () { }
    
}
