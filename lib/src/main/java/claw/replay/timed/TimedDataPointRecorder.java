package claw.replay.timed;

import java.util.ArrayList;
import java.util.List;

/**
 * A buffer which can be used to record {@link TimedDataPoint}s for use with {@link TimedControlAction}s.
 * For example, a {@code TimedDataPointRecorder} could be used to record the position of the robot over a period of time,
 * so that those movements can be replayed.
 */
public class TimedDataPointRecorder <T extends DataPoint<T>> {
    
    private final ArrayList<TimedDataPoint<T>> dataPoints = new ArrayList<>();
    private final int maxBufferLength;
    
    private boolean recordingEnabled = false;
    private long recordingStartTime;
    
    public TimedDataPointRecorder (int maxBufferLength) {
        this.maxBufferLength = maxBufferLength;
        stopRecording();
    }
    
    private synchronized void reset () {
        recordingStartTime = System.currentTimeMillis();
        dataPoints.clear();
    }
    
    /**
     * Reset the recording state and enable recording.
     */
    public void startRecording () {
        recordingEnabled = true;
        reset();
    }
    
    /**
     * Stop recording, clearing the internal recording buffer.
     */
    public void stopRecording () {
        recordingEnabled = false;
        reset();
    }
    
    private long getTime () {
        return System.currentTimeMillis() - recordingStartTime;
    }
    
    /**
     * Add a {@link DataPoint} to the buffer of timed data points. If recording is disabled,
     * the data point will be ignored.
     * @param dataPoint
     */
    public synchronized void addDataPoint (DataPoint<T> dataPoint) {
        if (!recordingEnabled) return;
        
        if (dataPoints.size() == maxBufferLength)
            dataPoints.remove(0);
        
        dataPoints.add(new TimedDataPoint<T>(getTime(), dataPoint));
    }
    
    /**
     * Return a clone of the internal buffer of {@link TimedDataPoint}s and stop recording.
     * @return
     */
    @SuppressWarnings("unchecked")
    public synchronized List<TimedDataPoint<T>> getDataBuffer () {
        List<TimedDataPoint<T>> buffer = (List<TimedDataPoint<T>>)dataPoints.clone();
        stopRecording();
        return buffer;
    }
    
}
