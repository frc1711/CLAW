package claw.hardware.swerve.auto;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class SwerveTrajectoryController {
    
    private final PIDController xController, yController, thetaController;
    
    public SwerveTrajectoryController (
        PIDController xController,
        PIDController yController,
        PIDController thetaController
    ) {
        this.xController = xController;
        this.yController = yController;
        this.thetaController = thetaController;
    }
    
    public void reset () {
        xController.reset();
        yController.reset();
        thetaController.reset();
    }
    
    public ChassisSpeeds getSpeeds (Pose2d currentPose, SwerveTrajectoryPoint trajectoryPoint) {
        return getSpeeds(currentPose, trajectoryPoint.getDesiredPose(), trajectoryPoint.getDesiredSpeeds());
    }
    
    public ChassisSpeeds getSpeeds (Pose2d currentPose, Pose2d trajectoryPose, ChassisSpeeds trajectorySpeeds) {
        ChassisSpeeds offsetCorrection = getOffsetCorrection(currentPose, trajectoryPose);
        
        return new ChassisSpeeds(
            trajectorySpeeds.vxMetersPerSecond + offsetCorrection.vxMetersPerSecond,
            trajectorySpeeds.vyMetersPerSecond + offsetCorrection.vyMetersPerSecond,
            trajectorySpeeds.omegaRadiansPerSecond + offsetCorrection.omegaRadiansPerSecond
        );
    }
    
    private ChassisSpeeds getOffsetCorrection (Pose2d currentPose, Pose2d trajectoryPose) {
        
        double xCorrection = xController.calculate(currentPose.getX(), trajectoryPose.getX());
        double yCorrection = yController.calculate(currentPose.getY(), trajectoryPose.getY());
        
        double offsetTheta = trajectoryPose.getRotation().getRadians() - currentPose.getRotation().getRadians();
        offsetTheta = MathUtil.inputModulus(offsetTheta, -Math.PI, Math.PI);
        double thetaCorrection = thetaController.calculate(0, offsetTheta);
        
        return new ChassisSpeeds(xCorrection, yCorrection, thetaCorrection);
        
    }
    
}
