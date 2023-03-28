package claw.rct.network.low.concurrency;

import java.util.concurrent.locks.ReentrantLock;

public class Waiter {
    
    private final ReentrantLock isWaitingLock = new ReentrantLock();
    private final Object waitObject = new Object();
    private boolean stopWaiting = false;
    
    public void resume () {
        synchronized (waitObject) {
            waitObject.notifyAll();
        }
    }
    
    public void pause () {
        pauseUntil(0);
    }
    
    public void pause (long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        
        pauseUntil(System.currentTimeMillis() + durationMillis);
    }
    
    private void pauseUntil (long targetTime) {
        isWaitingLock.lock();
        stopWaiting = false;
        
        long waitTime = getWaitTime(targetTime);
        
        while (waitTime > 0 && !stopWaiting) {
            try {
                synchronized (waitObject) {
                    waitObject.wait(waitTime);
                }
            } catch (InterruptedException e) { }
            
            waitTime = getWaitTime(targetTime);
        }
        
        isWaitingLock.unlock();
    }
    
    private static long getWaitTime (long targetTime) {
        return targetTime == 0 ? Long.MAX_VALUE : targetTime - System.currentTimeMillis();
    }
    
}
