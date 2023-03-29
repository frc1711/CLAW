package claw.actions.compositions;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import claw.actions.Action;

public interface ActionCompositionContext {
    
    public default void useContext () throws CompositionCanceledException {
        if (isTerminated()) {
            throw getTerminatedException();
        }
    }
    
    public default RuntimeException getTerminatedException () {
        return new CompositionCanceledException();
    }
    
    public boolean isTerminated ();
    public void terminate ();
    
    public static class ActionRunnerContext implements ActionCompositionContext {
        
        private boolean terminated = false;
        private Optional<Action> lastAction = Optional.empty();
        
        @Override
        public final boolean isTerminated () {
            return terminated;
        }
        
        @Override
        public final void terminate () {
            if (isTerminated()) return;
            terminated = true;
            lastAction.ifPresent(Action::cancel);
        }
        
        public final void runAction (Action action) {
            useContext();
            lastAction = Optional.of(action);
            action.run();
        }
        
    }
    
    public static <T extends ActionCompositionContext> Action compose (Supplier<T> contextSupplier, Consumer<T> composition) {
        return new Action() {
            
            private Optional<T> context = Optional.empty();
            
            @Override
            public void runAction () {
                context = Optional.of(contextSupplier.get());
                try {
                    composition.accept(context.get());
                } catch (CompositionCanceledException e) { }
            }
            
            @Override
            public void cancelRunningAction () {
                context.ifPresent(ActionCompositionContext::terminate);
            }
            
        };
    }
    
    public static class CompositionCanceledException extends RuntimeException {
        public CompositionCanceledException () {
            super("Action composition has been canceled, exit the composition as soon as possible");
        }
    }
    
}
