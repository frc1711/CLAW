package claw.hardware.swerve.auto;

import claw.math.Vector;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.interpolation.Interpolatable;
import edu.wpi.first.math.numbers.N2;

public class SwerveTrajectoryPoint implements Interpolatable<SwerveTrajectoryPoint> {
    
    public final Vector<N2> positionMeters, velocityMetersPerSec;
    public final double angleRadians, angularVelocityRadiansPerSec;
    
    public SwerveTrajectoryPoint (
        Vector<N2> positionMeters,
        double angleRadians,
        Vector<N2> velocityMetersPerSec,
        double angularVelocityRadiansPerSec
    ) {
        this.positionMeters = positionMeters;
        this.angleRadians = angleRadians;
        this.velocityMetersPerSec = velocityMetersPerSec;
        this.angularVelocityRadiansPerSec = angularVelocityRadiansPerSec;
    }
    
    @Override
    public SwerveTrajectoryPoint interpolate (SwerveTrajectoryPoint other, double t) {
        return new SwerveTrajectoryPoint(
            positionMeters.interpolate(other.positionMeters, t),
            MathUtil.interpolate(angleRadians, other.angleRadians, t),
            velocityMetersPerSec.interpolate(other.velocityMetersPerSec, t),
            MathUtil.interpolate(angularVelocityRadiansPerSec, other.angularVelocityRadiansPerSec, t)
        );
    }
    
}
