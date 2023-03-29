package claw.actions.compositions;

import java.util.function.Consumer;

import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestComposer {
    
    public static Command compose (ConsoleManager console, Consumer<SubsystemTestCompositionContext> composition) {
        return ActionCompositionContext.compose(
            () -> new SubsystemTestCompositionContext(console),
            composition
        ).toCommand();
    }
    
    private SubsystemTestComposer () { }
    
}
