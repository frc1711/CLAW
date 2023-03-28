package claw.actions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import claw.actions.ActionCompositionContext.ActionCompositionCanceledException;

public class ActionComposer <T extends ActionCompositionContext> extends Action {
    
    private final T ctx;
    private final Runnable runComposition;
    
    public ActionComposer (Supplier<T> ctxSupplier, Consumer<T> makeComposition) {
        // TODO: Prevent tons of extra threads from being generated (somewhere in code, not sure where)
        this.ctx = ctxSupplier.get();
        this.runComposition = () -> {
            try {
                makeComposition.accept(ctx);
            } catch (ActionCompositionCanceledException e) { }
        };
    }
    
    public void runAction () {
        runComposition.run();
    }
    
    public void cancelRunningAction () {
        ctx.terminate();
    }
    
}
