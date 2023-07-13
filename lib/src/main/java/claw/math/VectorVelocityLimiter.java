package claw.math;

import edu.wpi.first.math.Num;
import edu.wpi.first.wpilibj.Timer;

/**
 * A {@link Transform} acting on {@link Vector}s which limits the magnitude of the vector's velocity over time.
 * This filter works for vectors of arbitrary dimensionality.
 */
public class VectorVelocityLimiter <N extends Num> {
    
    private final double maxVelocity;
    
    private Vector<N> vectorValue;
    private double lastTimestamp;
    
    /**
     * Create a new {@link VectorVelocityLimiter} with an initial vector value.
     * @param initialValue  The initial {@link Vector} value of this filter.
     * @param maxVelocity   The maximum velocity of this filter's vector value, measured in units
     * per second.
     */
    public VectorVelocityLimiter (Vector<N> initialValue, double maxVelocity) {
        this.maxVelocity = maxVelocity;
        reset(initialValue);
    }
    
    /**
     * Resets the internal timer, so the next application of the filter (in measuring velocity)
     * will be relative to the current time.
     * @param newValue  The new {@link Vector} state of the filter.
     */
    public void reset () {
        reset(vectorValue);
    }
    
    /**
     * Resets the filter to a new vector value. This will also reset the internal timer,
     * so the next application of the filter (in measuring velocity) will be relative to
     * the current time.
     * @param newValue  The new {@link Vector} state of the filter.
     */
    public void reset (Vector<N> newValue) {
        reset(newValue, Timer.getFPGATimestamp());
    }
    
    private void reset (Vector<N> newValue, double timestamp) {
        lastTimestamp = timestamp;
        vectorValue = newValue;
    }
    
    /**
     * Apply the velocity filter to the given vector, limiting the rate of change of the filter's vector value.
     * @param inputVector   The {@link Vector} input, which will be compared to the current value
     * of the filter to limit the velocity.
     * @return              The vector output of the filter after limiting the velocity of the filter's value.
     */
    public Vector<N> calculate (Vector<N> inputVector) {
        
        // Get the new timestamp and time delta
        double newTimestamp = Timer.getFPGATimestamp();
        double deltaTime = newTimestamp - lastTimestamp;
        
        // Get the desired vector delta and the velocity corresponding to that delta
        Vector<N> desiredVectorDelta = inputVector.subtract(vectorValue);
        double desiredVelocity = desiredVectorDelta.getMagnitude() / deltaTime;
        
        // If the desired velocity is higher than the max velocity, scale the desired vector delta
        // so that it conforms with the max velocity
        Vector<N> filteredVectorDelta = (desiredVelocity > maxVelocity)
            ? desiredVectorDelta.scale(maxVelocity / desiredVelocity)
            : desiredVectorDelta;
        
        // Get the new filtered vector value
        Vector<N> filteredVectorValue = vectorValue.add(filteredVectorDelta);
        
        // Reset to the new filtered value and the current timestamp
        reset(filteredVectorValue, newTimestamp);
        
        // Return the new value
        return filteredVectorValue;
        
    }
    
}

