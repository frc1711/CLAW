package claw.hardware.swerve.auto;

import claw.math.Vector;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.interpolation.Interpolatable;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
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
    
    /**
     * Field-relative speeds
     */
    public ChassisSpeeds getDesiredSpeeds () {
        return new ChassisSpeeds(
            velocityMetersPerSec.getX(),
            velocityMetersPerSec.getY(),
            angularVelocityRadiansPerSec
        );
    }
    
    public Pose2d getDesiredPose () {
        return new Pose2d(
            positionMeters.getX(),
            positionMeters.getY(),
            new Rotation2d(angleRadians)
        );
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
