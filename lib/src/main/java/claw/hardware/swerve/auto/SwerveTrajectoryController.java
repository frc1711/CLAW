package claw.hardware.swerve.auto;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class SwerveTrajectoryController {
    
    private final PIDController distanceController, thetaController;
    
    public SwerveTrajectoryController (
        PIDController distanceController,
        PIDController thetaController
    ) {
        this.distanceController = distanceController;
        this.thetaController = thetaController;
    }
    
    public void reset () {
        distanceController.reset();
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
        
        // Get a translational offset from the current pose to the trajectory
        Translation2d translationOffset = trajectoryPose.getTranslation().minus(currentPose.getTranslation());
        
        // Apply the distance controller to the magnitude of this offset translation
        double distCorrectionMag = distanceController.calculate(0, translationOffset.getNorm());
        
        // Separate x and y correction according to the angle of the translation
        double xCorrection = translationOffset.getAngle().getCos() * distCorrectionMag;
        double yCorrection = translationOffset.getAngle().getSin() * distCorrectionMag;
        
        // Get the rotational offset from the desired pose
        double offsetTheta = trajectoryPose.getRotation().getRadians() - currentPose.getRotation().getRadians();
        offsetTheta = MathUtil.inputModulus(offsetTheta, -Math.PI, Math.PI);
        
        // Apply the theta controller to this rotational offset
        double thetaCorrection = thetaController.calculate(0, offsetTheta);
        
        // Return the correctional speeds
        return new ChassisSpeeds(xCorrection, yCorrection, thetaCorrection);
        
    }
    
}
