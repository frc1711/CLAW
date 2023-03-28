package claw.actions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ActionComposer <T extends ActionCompositionContext> extends Action {
    
    private final T ctx;
    private final Runnable runComposition;
    
    public ActionComposer (Supplier<T> ctxSupplier, Consumer<T> makeComposition) {
        this.ctx = ctxSupplier.get();
        this.runComposition = () -> makeComposition.accept(ctx);
    }
    
    public void runAction () {
        runComposition.run();
    }
    
    public void cancelRunningAction () {
        ctx.terminate();
    }
    
}
