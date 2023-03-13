package claw.math;

import edu.wpi.first.math.filter.Debouncer;

/**
 * An extension of the WPILib {@link Debouncer} allowing for a different rising edge and falling edge debounce times.
 * This {@code DualDebouncer} also allows for greater control over the internal state of the debouncer. For this reason,
 * it may be useful to use a {@code DualDebouncer} even if different rising edge and falling edge debounce times are not
 * required.
 */
public class DualDebouncer extends Debouncer {
    
    private final double fallingEdgeTime, risingEdgeTime;
    
    /**
     * The current state of the debouncer (the "baseline", because the internalDebouncer is set up to
     * only debounce the edge which would change this baselineState)
     */
    private boolean baselineState;
    
    /**
     * The internal debouncer, which is reset whenever the baseline changes (i.e. there will be a different
     * internalDebouncer for falling edge vs. rising edge debouncing)
     */
    private Debouncer internalDebouncer;
    
    /**
     * Create a new {@link DualDebouncer} with separate falling edge and rising edge debounce times.
     * @param baselineState     The initial state of this debouncer.
     * @param fallingEdgeTime   The number of seconds the input must be changed to {@code false} when the
     * baseline is {@code true} in order for the baseline to change.
     * @param risingEdgeTime    The number of seconds the input must be changed to {@code true} when the
     * baseline is {@code false} in order for the baseline to change.
     */
    public DualDebouncer (boolean baselineState, double fallingEdgeTime, double risingEdgeTime) {
        // This class only extends Debouncer so that it can be used in place
        // of a WPILib Debouncer. Otherwise, it actually doesn't use inheritance
        // to control the debounce filter at all.
        super(0);
        
        this.fallingEdgeTime = fallingEdgeTime;
        this.risingEdgeTime = risingEdgeTime;
        
        // Reset to the given baselineState
        resetToBaseline(baselineState);
    }
    
    /**
     * Create a new {@link DualDebouncer} with a given debounceTime to use for both the rising and falling edges.
     * @param baselineState     The initial state of this debouncer.
     * @param debounceTime      The number of seconds the input must be different than the baseline for before the
     * baseline changes.
     */
    public DualDebouncer (boolean baselineState, double debounceTime) {
        this(baselineState, debounceTime, debounceTime);
    }
    
    /**
     * Reset the timer of the debouncer without changing the current baseline state.
     */
    public void resetTimer () {
        resetToBaseline(baselineState);
    }
    
    /**
     * Reset the timer of the debouncer and change the baseline state.
     * @param newBaseline   The new baseline state of the debouncer.
     */
    public void resetToBaseline (boolean newBaseline) {
        // Reset the baseline state
        baselineState = newBaseline;
        
        // Set the internalDebouncer for the new baseline
        internalDebouncer = newBaseline
            // If the newBaseline is true, we debounce the falling edge
            ? new Debouncer(fallingEdgeTime, DebounceType.kFalling)
            
            // If the newBaseline is false, we debounce the rising edge
            : new Debouncer(risingEdgeTime, DebounceType.kRising);
    }
    
    @Override
    public boolean calculate (boolean input) {
        
        // Again, we actually don't use any details from the superclass implementation of the debouncer.
        // All debounce control is internal. We only extend the WPILib Debouncer so this DualDebouncer
        // can be used in any place the WPILib Debouncer can be used
        
        // If the internalDebouncer indicates that the baseline state must change,
        // reset to the new baseline
        if (internalDebouncer.calculate(input) != baselineState) {
            resetToBaseline(!baselineState);
        }
        
        return baselineState;
        
    }
    
}
