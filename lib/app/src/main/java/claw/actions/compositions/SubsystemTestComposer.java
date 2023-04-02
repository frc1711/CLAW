package claw.actions.compositions;

import claw.actions.compositions.Context.Operation;
import claw.rct.network.low.ConsoleManager;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestComposer {
    
    public static Command compose (
        ConsoleManager console,
        CLAWSubsystem subsystem,
        Operation<SubsystemTestCompositionContext<?>> composition
    ) {
        return Context.compose(
            () -> new SubsystemTestCompositionContext<>(console, subsystem),
            composition
        ).toCommand();
    }
    
    private SubsystemTestComposer () { }
    
}
