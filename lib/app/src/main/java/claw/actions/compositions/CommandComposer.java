package claw.actions.compositions;

import java.util.function.Consumer;

import edu.wpi.first.wpilibj2.command.Command;

public class CommandComposer {
    
    public static Command compose (Consumer<CommandCompositionContext> composition) {
        return ActionCompositionContext.compose(CommandCompositionContext::new, composition).toCommand();
    }
    
    private CommandComposer () { }
    
}
