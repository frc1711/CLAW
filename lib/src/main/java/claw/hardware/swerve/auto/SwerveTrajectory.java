package claw.hardware.swerve.auto;

public interface SwerveTrajectory {
    public double getTotalDuration ();
    public SwerveTrajectoryPoint getPoint (double time);
}
