package claw.replay.timed;

import java.util.Set;

import claw.replay.RobotActionRecord;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;

public abstract class TimedControlAction <T extends DataPoint<T>> implements RobotActionRecord {
    
    private final TimedDataPoint<T>[] dataPoints;
    
    public TimedControlAction (TimedDataPoint<T>[] dataPoints) {
        this.dataPoints = dataPoints;
        
    }
    
    public abstract void moveToState (DataPoint<T> desiredState);
    public abstract void stopSubsystems ();
    public abstract Set<Subsystem> getSubsystems ();
    
    @Override
    public Command toReplayCommand () {
        return new TimedControlCommand();
    }
    
    private class TimedControlCommand implements Command {
        
        private long initializeTime;
        
        private long getTime () {
            return System.currentTimeMillis() - initializeTime;
        }
        
        private DataPoint<T> getInterpolatedDataPoint () {
            int nextDataPointIndex = dataPoints.length - 1;
            
            for (int i = 0; i < dataPoints.length; i ++) {
                if (dataPoints[i].timeMillis > getTime()) {
                    nextDataPointIndex = i;
                    break;
                }
            }
            
            TimedDataPoint<T> nextDataPoint = dataPoints[nextDataPointIndex];
            
            if (nextDataPoint.timeMillis < getTime() || nextDataPointIndex == 0) {
                return nextDataPoint.data;
            } else {
                TimedDataPoint<T> lastDataPoint = dataPoints[nextDataPointIndex - 1];
                return lastDataPoint.interpolateWithTime(nextDataPoint, getTime());
            }
        }
        
        @Override
        public void initialize () {
            stopSubsystems();
            initializeTime = System.currentTimeMillis();
        }
        
        @Override
        public void execute () {
            moveToState(getInterpolatedDataPoint());
        }
        
        @Override
        public void end (boolean interrupted) {
            stopSubsystems();
        }
        
        @Override
        public Set<Subsystem> getRequirements () {
            return getSubsystems();
        }
        
        @Override
        public boolean isFinished () {
            TimedDataPoint<T> lastPoint = dataPoints[dataPoints.length - 1];
            return getTime() > lastPoint.timeMillis;
        }
        
    }
    
}
