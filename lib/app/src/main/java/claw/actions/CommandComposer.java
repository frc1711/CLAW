package claw.actions;

import java.util.function.Consumer;

import edu.wpi.first.wpilibj2.command.Command;

public class CommandComposer {
    
    public static Command getComposition (Consumer<CommandCompositionContext> makeComposition) {
        return new CommandActionWrapper(new ActionComposer<>(CommandCompositionContext::new, makeComposition));
    }
    
}
