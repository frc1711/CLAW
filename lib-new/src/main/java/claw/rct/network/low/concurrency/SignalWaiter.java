package claw.rct.network.low.concurrency;

import java.util.Optional;

/**
 * A concurrency helper class which can send and receive signals between threads.
 */
public class SignalWaiter <T> {
    
    private final Waiter waiter = new Waiter();
    private Optional<T> signal = Optional.empty();
    
    /**
     * Wait for the next signal to be sent with {@link #receiveSignal(Object)}.
     * @return  The received signal, or {@link Optional#empty()} if {@link #kill()} was
     * called.
     */
    public synchronized Optional<T> awaitSignal () {
        signal = Optional.empty();
        waiter.pause();
        return signal;
    }
    
    /**
     * Wait for the next signal to be sent with {@link #receiveSignal(Object)}, for a given
     * timeout.
     * @return  The received signal, or {@link Optional#empty()} if {@link #kill()} was
     * called or if the timeout was reached.
     */
    public synchronized Optional<T> awaitSignal (long timeoutMillis) {
        signal = Optional.empty();
        waiter.pause(timeoutMillis);
        return signal;
    }
    
    /**
     * Receive the signal, so that any {@link #awaitSignal()} calls will awake with the sent signal.
     * @param signal    The signal to send.
     */
    public void receiveSignal (T signal) {
        this.signal = Optional.of(signal);
        waiter.resume();
    }
    
    /**
     * Kill the waiter so that any {@link #awaitSignal()} calls will return an empty signal.
     */
    public void kill () {
        waiter.resume();
    }
    
}
