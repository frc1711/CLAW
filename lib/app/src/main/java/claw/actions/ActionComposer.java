package claw.actions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import claw.actions.ActionCompositionContext.ActionCompositionCanceledException;

public class ActionComposer <T extends ActionCompositionContext> {
    
    private final Consumer<T> composition;
    
    public ActionComposer (Consumer<T> composition) {
        // TODO: Prevent tons of extra threads from being generated (somewhere in code, not sure where)
        this.composition = composition;
    }
    
    public Action getComposition (Supplier<T> contextSupplier) {
        return new CompositionAction(contextSupplier, composition);
    }
    
    private class CompositionAction extends Action {
        
        private final Consumer<T> composition;
        private final Supplier<T> contextSupplier;
        
        private T activeContext;
        
        public CompositionAction (Supplier<T> contextSupplier, Consumer<T> composition) {
            this.composition = composition;
            this.contextSupplier = contextSupplier;
        }
        
        @Override
        public void runAction () {
            activeContext = contextSupplier.get();
            
            try {
                composition.accept(activeContext);
            } catch (ActionCompositionCanceledException e) { }
        }
        
        @Override
        public void cancelRunningAction () {
            if (activeContext != null) activeContext.terminate();
        }
        
    }
    
}
