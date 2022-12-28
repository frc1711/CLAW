package rct.network.low;

/**
 * A helper class which allows for {@link Waiter#receive(Object)} and {@link Waiter#waitForValue(long)} to be called on
 * different threads, where {@code waitForValue} waits a given number of milliseconds (blocking) for a value to be received
 * through {@code receive}.
 */
public class Waiter <T> {
        
    private final Object waiterObject = new Object();
    private boolean isWaiting = false;
    private boolean hasReceived = false;
    private T valueReceived;
    
    /**
     * Receives a value so that {@link Waiter#waitForValue(long)} will finish and return the given value.
     * @param value The value to send to any waiting {@link Waiter#waitForValue(long)} calls.
     */
    public void receive (T value) {
        // Do nothing if we are not waiting for a new value
        if (!isWaiting) return;
        
        // Set the value field to match the provided value
        valueReceived = value;
        
        // Set the hasReceived field to indicate a value has been received
        hasReceived = true;
        
        // Notify the waiter object so that threads waiting for this value will start up again
        synchronized (waiterObject) {
            waiterObject.notifyAll();
        }
    }
    
    /**
     * Waits a given number of milliseconds for a value to be received by {@link Waiter#receive(Object)}.
     * @param millis                    The duration of time, in milliseconds, to wait for a received value for.
     * @return                          The value received through the next {@link Waiter#receive(Object)} call
     * within the specified duration.
     * @throws NoValueReceivedException If no value is received within the given duration.
     */
    public T waitForValue (long millis) throws NoValueReceivedException {
        // Set isWaiting to true so that values passed into Waiter.receive() will be processed
        isWaiting = true;
        
        // Set the hasReceived field to false so that we can later check if it has been set to true by Waiter.receive()
        hasReceived = false;
        
        // Wait for the given timeout period, or until notified by Waiter.receive()
        synchronized (waiterObject) {
            try {
                waiterObject.wait(millis);
            } catch (InterruptedException e) { }
        }
        
        // Set isWaiting to false so that new values passed in to Waiter.receive() will not be processed
        isWaiting = false;
        
        // If no value has been received, throw an exception
        if (!hasReceived)
            throw new NoValueReceivedException();
        
        // Otherwise, the new value can be returned
        return valueReceived;
    }
    
    /**
     * Gets whether or not the {@link Waiter} is awaiting a value.
     * @return {@code true} if there is a {@link Waiter#waitForValue(long)} call blocking, {@code false} otherwise.
     */
    public boolean isWaiting () {
        return isWaiting;
    }
    
    /**
     * An exception thrown if a {@link Waiter#waitForValue(long)}'s timeout is reached without a value
     * being received through {@link Waiter#receive(Object)}.
     */
    public static class NoValueReceivedException extends Exception { }
    
}
