package claw.replay.action;

import java.io.Serializable;

public interface DataPoint <T extends DataPoint<T>> extends Serializable {
    public abstract T interpolate (DataPoint<T> otherPoint, double p);
}
