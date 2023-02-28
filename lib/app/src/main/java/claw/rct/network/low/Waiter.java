package claw.rct.network.low;

import java.util.Optional;

/**
 * A helper class which allows for {@link Waiter#receive(Object)} and {@link Waiter#waitForValue(long)} to be called on
 * different threads, where {@code waitForValue} waits a given number of milliseconds (blocking) for a value to be received
 * through {@code receive}.
 */
public class Waiter <T> {
        
    private final Object waiterObject = new Object();
    private boolean isWaiting = false;
    
    private Optional<T> valueReceived;
    private boolean stopWaiting = false;
    
    /**
     * Receives a value so that {@link Waiter#waitForValue()} will finish and return the given value.
     * @param value The value to send to any waiting {@link Waiter#waitForValue()} calls.
     */
    public void receive (T value) {
        // Do nothing if we are not waiting for a new value
        if (!isWaiting) return;
        
        // Set the value field to match the provided value
        valueReceived = Optional.of(value);
        
        // Start up waiting threads
        startUpWaitingThreads();
    }
    
    /**
     * Kills the waiter so that any running {@link Waiter#waitForValue()} will immediately throw a
     * {@link NoValueReceivedException}.
     */
    public void kill () {
        // Do nothing if we are not waiting for a new value
        if (!isWaiting) return;
        
        // Start up waiting threads
        startUpWaitingThreads();
    }
    
    private void startUpWaitingThreads () {
        // Set stopWaiting so awoken threads will not start waiting again
        stopWaiting = true;
        
        // Notify the waiter object so that threads waiting for this value will start up again
        synchronized (waiterObject) {
            waiterObject.notifyAll();
        }
    }
    
    /**
     * Waits indefinitely for a value to be received by {@link Waiter#receive(Object)}.
     * @throws NoValueReceivedException If {@link Waiter#kill()} is called before an object is received.
     */
    public synchronized T waitForValue () throws NoValueReceivedException {
        return waitForValue(-1);
    }
    
    /**
     * Waits a given number of milliseconds for a value to be received by {@link Waiter#receive(Object)}.
     * @param millis                    The duration of time, in milliseconds, to wait for a received value for.
     * If {@code millis} is {@code -1}, the waiter will wait indefinitely (until either a value is received
     * or the waiter is killed).
     * @return                          The value received through the next {@link Waiter#receive(Object)} call
     * within the specified duration.
     * @throws NoValueReceivedException If no value is received within the given duration or if the {@link Waiter#kill()}
     * is called.
     */
    public synchronized T waitForValue (long millis) throws NoValueReceivedException {
        // Set isWaiting to true so that values passed into Waiter.receive() will be processed
        isWaiting = true;
        
        // Set the hasReceived field to false so that we can later check if it has been set to true by Waiter.receive()
        valueReceived = Optional.empty();
        
        // Wait for the given timeout period, or until notified by Waiter.receive()
        stopWaiting = false;
        while (!stopWaiting) {
            synchronized (waiterObject) {
                try {
                    if (millis == -1)
                        waiterObject.wait();
                    else
                        waiterObject.wait(millis);
                } catch (InterruptedException e) { }
            }
        }
        
        // Set isWaiting to false so that new values passed in to Waiter.receive() will not be processed
        isWaiting = false;
        
        // If no value has been received, throw an exception
        if (valueReceived.isEmpty())
            throw new NoValueReceivedException();
        
        // Otherwise, the new value can be returned
        return valueReceived.get();
    }
    
    /**
     * Gets whether or not the {@link Waiter} is awaiting a value.
     * @return {@code true} if there is a {@link Waiter#waitForValue()} call blocking, {@code false} otherwise.
     */
    public boolean isWaiting () {
        return isWaiting;
    }
    
    /**
     * An exception thrown if a {@link Waiter#waitForValue()}'s timeout is reached without a value
     * being received through {@link Waiter#receive(Object)}.
     */
    public static class NoValueReceivedException extends Exception { }
    
}
