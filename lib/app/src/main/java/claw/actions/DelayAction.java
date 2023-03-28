package claw.actions;

import claw.rct.network.low.concurrency.Waiter;

public class DelayAction extends Action {
    
    private final double durationSecs;
    private final Waiter cancelWaiter = new Waiter();
    
    public DelayAction (double durationSecs) {
        this.durationSecs = durationSecs;
    }
    
    @Override
    protected void runAction () {
        cancelWaiter.pause((long)(1000 * durationSecs));
    }
    
    @Override
    protected void cancelRunningAction () {
        cancelWaiter.resume();
    }
    
}
