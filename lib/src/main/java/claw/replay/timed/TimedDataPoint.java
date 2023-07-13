package claw.replay.timed;

import java.io.Serializable;

/**
 * Represents a {@link DataPoint} with a associated time relative to some starting time.
 */
public class TimedDataPoint <T extends DataPoint<T>> implements Serializable {
    public final long timeMillis;
    public final DataPoint<T> data;
    
    public TimedDataPoint (long timeMillis, DataPoint<T> data) {
        this.timeMillis = timeMillis;
        this.data = data;
    }
    
    public DataPoint<T> interpolateWithTime (TimedDataPoint<T> next, long time) {
        long timeInterval = next.timeMillis - timeMillis;
        double p = (time - timeMillis) / (double)timeInterval;
        return data.interpolate(next.data, p);
    }
}
