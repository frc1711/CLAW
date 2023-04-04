package claw.actions;

/**
 * TODO: Untested code
 */
class ParallelAction extends Action {
    
    private final Action[] actions;
    
    public ParallelAction (Action... actions) {
        // TODO: THIS IS NOT WORKING
        this.actions = actions;
    }
    
    @Override
    public void runAction () {
        if (actions.length >= 1) {
            // Create and start threads for each parallel action
            Thread[] threads = new Thread[actions.length];
            for (int i = 0; i < threads.length; i ++) {
                threads[i] = new Thread(actions[i]::run);
                threads[i].start();
            }
            
            // Wait for all threads to finish
            for (Thread thread : threads) {
                while (thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) { }
                }
            }
        } else if (actions.length == 1) {
            actions[0].run();
        }
    }
    
    @Override
    public void cancelRunningAction () {
        // Cancel all running actions
        for (Action action : actions) {
            action.cancel();
        }
    }
    
}
