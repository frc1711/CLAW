package claw.actions;

import java.util.function.Consumer;

import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestComposer extends ActionComposer<SubsystemTestCompositionContext> {
    
    public static Command compose (ConsoleManager console, Consumer<SubsystemTestCompositionContext> composition) {
        return new SubsystemTestComposer(composition)
            .getComposition(() -> new SubsystemTestCompositionContext(console))
            .toCommand();
    }
    
    public SubsystemTestComposer (Consumer<SubsystemTestCompositionContext> composition) {
        super(composition);
    }
    
}
