package claw.rct.base.network.low.concurrency;

import java.util.concurrent.locks.ReentrantLock;

/**
 * A concurrency helper class which can {@link #pause(long)} for given durations (or indefinitely)
 * and {@link #resume()}.
 */
public class Waiter {
    
    private final ReentrantLock isWaitingLock = new ReentrantLock();
    private final Object waitObject = new Object();
    private boolean resumeSignalReceived = false;
    
    /**
     * Pause the thread until the duration has passed.
     * @param durationMillis    The duration to wait for. This cannot be negative.
     */
    public static void pauseThread (long durationMillis) {
        new Waiter().pause(durationMillis);
    }
    
    /**
     * Interrupt a {@link #pause()} or {@link #pause(long)} wait, so that the {@link Waiter} wakes up immediately.
     */
    public void resume () {
        synchronized (waitObject) {
            waitObject.notifyAll();
            resumeSignalReceived = true;
        }
    }
    
    /**
     * Pause the waiter indefinitely, until a {@link #resume()} call is made.
     */
    public void pause () {
        pauseUntil(0);
    }
    
    /**
     * Pause the waiter for the given duration, in milliseconds. The waiter will only resume when
     * the duration is reached, or if {@link #resume()} is called on the waiter.
     * @param durationMillis    The duration to wait for, measured in milliseconds. This cannot
     * be negative.
     * @return                  Whether the waiter resumed due to a {@link #resume()} call
     * (rather than resuming because the timeout was reached).
     */
    public boolean pause (long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }
        
        return pauseUntil(System.currentTimeMillis() + durationMillis);
    }
    
    /**
     * Pause until the given target time in milliseconds. If the target time is zero, it will
     * pause indefinitely. This returns whether or not the waiter was woken up with a {@link #resume()} call.
     */
    private boolean pauseUntil (long targetTime) {
        isWaitingLock.lock();
        resumeSignalReceived = false;
        
        // Get the amount of time to wait for given the target time
        long timeToWait = getWaitTime(targetTime);
        
        // Wait until the time to wait is up or until a resume signal is received
        while (timeToWait > 0 && !resumeSignalReceived) {
            // Attempt to wait for the timeToWait
            try {
                synchronized (waitObject) {
                    waitObject.wait(timeToWait);
                }
            } catch (InterruptedException e) { }
            
            // Adjust the timeToWait in case the thread was awoken early
            timeToWait = getWaitTime(targetTime);
        }
        
        // Reset the resume signal
        boolean wokenUp = resumeSignalReceived;
        resumeSignalReceived = false;
        
        // Unlock the waiter lock so the waiter can be reused
        isWaitingLock.unlock();
        
        return wokenUp;
    }
    
    /**
     * Returns the number of milliseconds to wait for to reach the target time. If the target time is zero,
     * this will return the maximum value of a {@code long}.
     */
    private static long getWaitTime (long targetTime) {
        return targetTime == 0 ? Long.MAX_VALUE : targetTime - System.currentTimeMillis();
    }
    
}
