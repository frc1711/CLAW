package claw.actions;

import claw.rct.network.low.concurrency.Waiter;
import claw.rct.network.low.concurrency.Waiter.NoValueReceivedException;

public class DelayAction extends Action {
    
    private final double durationSecs;
    private final Waiter<Object> cancelWaiter = new Waiter<>();
    
    public DelayAction (double durationSecs) {
        this.durationSecs = durationSecs;
    }
    
    @Override
    protected void runAction () {
        try {
            cancelWaiter.waitForValue((long)(1000 * durationSecs));
        } catch (NoValueReceivedException e) { }
    }
    
    @Override
    protected void cancelRunningAction () {
        cancelWaiter.receive(new Object());
    }
    
}
