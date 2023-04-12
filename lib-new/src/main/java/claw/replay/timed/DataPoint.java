package claw.replay.timed;

import java.io.Serializable;

import edu.wpi.first.math.interpolation.Interpolatable;

public interface DataPoint <T extends DataPoint<T>> extends Serializable, Interpolatable<DataPoint<T>> { }
