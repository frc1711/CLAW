package claw.actions;

import java.util.function.Consumer;

import edu.wpi.first.wpilibj2.command.Command;

public class CommandComposer extends ActionComposer<CommandCompositionContext> {
    
    public static Command compose (Consumer<CommandCompositionContext> composition) {
        return new CommandComposer(composition).getComposition(CommandCompositionContext::new).toCommand();
    }
    
    public CommandComposer (Consumer<CommandCompositionContext> composition) {
        super(composition);
    }
    
}
