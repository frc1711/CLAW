package claw.actions;

import claw.rct.network.low.concurrency.ObjectWaiter;
import claw.rct.network.low.concurrency.ObjectWaiter.NoValueReceivedException;

public class DelayAction extends Action {
    
    private final double durationSecs;
    private final ObjectWaiter<Object> cancelObjectWaiter = new ObjectWaiter<>();
    
    public DelayAction (double durationSecs) {
        this.durationSecs = durationSecs;
    }
    
    @Override
    protected void runAction () {
        try {
            cancelObjectWaiter.waitForValue((long)(1000 * durationSecs));
        } catch (NoValueReceivedException e) { }
    }
    
    @Override
    protected void cancelRunningAction () {
        cancelObjectWaiter.receive(new Object());
    }
    
}
