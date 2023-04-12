package claw.actions.compositions;

import claw.actions.compositions.Context.Operation;
import edu.wpi.first.wpilibj2.command.Command;

public class CommandComposer {
    
    public static Command compose (Operation<CommandCompositionContext<?>> composition) {
        return Context.compose(CommandCompositionContext::new, composition).toCommand();
    }
    
    private CommandComposer () { }
    
}
