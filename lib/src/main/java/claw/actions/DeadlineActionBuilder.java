package claw.actions;

import java.util.Optional;

class DeadlineActionBuilder {
    
    private DeadlineActionBuilder () { }
    
    public static Action getAction (Action deadline, Action... runActions) {
        
        KillActionWrapper killWrapper = new KillActionWrapper(deadline);
        Action deadlineAction = Action.parallel(concatenate(killWrapper, runActions));
        killWrapper.setKillAction(deadlineAction);
        
        return deadlineAction;
        
    }
    
    private static Action[] concatenate (Action x, Action[] array) {
        Action[] newArray = new Action[array.length + 1];
        newArray[0] = x;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }
    
    private static class KillActionWrapper extends Action {
        
        private final Action action;
        private Optional<Action> killAction = Optional.empty();
        
        public KillActionWrapper (Action action) {
            this.action = action;
        }
        
        public void setKillAction (Action action) {
            this.killAction = Optional.of(action);
        }
        
        @Override
        public void runAction () {
            action.run();
            killAction.ifPresent(Action::cancel);
        }
        
        @Override
        public void cancelRunningAction () {
            action.cancel();
        }
        
    }
    
}
