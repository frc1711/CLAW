package claw.actions;

import java.util.function.Consumer;

import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj2.command.Command;

public class SubsystemTestComposer {
    
    public static Command getComposition (ConsoleManager console, Consumer<SubsystemTestCompositionContext> makeComposition) {
        return new CommandActionWrapper(new ActionComposer<SubsystemTestCompositionContext>(
            () -> new SubsystemTestCompositionContext(console),
            makeComposition
        ));
    }
    
}
