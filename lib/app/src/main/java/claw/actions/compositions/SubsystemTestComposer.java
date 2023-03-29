package claw.actions.compositions;

import java.util.function.Consumer;

import claw.rct.network.low.ConsoleManager;
import claw.subsystems.CLAWSubsystem;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestComposer {
    
    public static Command compose (
        ConsoleManager console,
        CLAWSubsystem subsystem,
        Consumer<SubsystemTestCompositionContext> composition
    ) {
        return ActionCompositionContext.compose(
            () -> new SubsystemTestCompositionContext(console, subsystem),
            composition
        ).toCommand();
    }
    
    private SubsystemTestComposer () { }
    
}
