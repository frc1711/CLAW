package claw.hardware.swerve;

import java.util.function.Supplier;

import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;

/**
 * A class which handles swerve drive kinematics and odometry for easy swerve control and implementation.
 */
public class SwerveDriveHandler {
    
    private final SwerveDriveKinematics kinematics;
    private final Supplier<Rotation2d> absoluteRotationSupplier;
    private final SwerveModuleBase[] swerveModules;
    private final Translation2d[] moduleTranslations;
    private final SwerveDrivePoseEstimator poseEstimator;
    
    /**
     * Create a new {@link SwerveDriveHandler}. Note that {@link #periodicUpdate()} must be called periodically
     * in order to get accurate pose estimates.
     * @param initialRobotPose          The robot's initial {@link Pose2d}.
     * @param absoluteRotationSupplier  A {@link Supplier} providing {@link Rotation2d} describing the robot's rotation.
     * This should never reset. Counter-clockwise must be positive.
     * @param swerveModules             The {@link SwerveModuleBase}s to control.
     */
    public SwerveDriveHandler (
        Pose2d initialRobotPose,
        Supplier<Rotation2d> absoluteRotationSupplier,
        SwerveModuleBase... swerveModules
    ) {
        this.swerveModules = swerveModules;
        
        // Get module translations
        moduleTranslations = new Translation2d[swerveModules.length];
        for (int i = 0; i < swerveModules.length; i ++) {
            moduleTranslations[i] = swerveModules[i].getTranslation();
        }
        
        // Get the kinematics class
        kinematics = new SwerveDriveKinematics(moduleTranslations);
        
        // Get the absolute rotation supplier
        this.absoluteRotationSupplier = absoluteRotationSupplier;
        
        // Get the pose estimator
        poseEstimator = new SwerveDrivePoseEstimator(
            kinematics,
            absoluteRotationSupplier.get(),
            getModulePositions(),
            initialRobotPose
        );
    }
    
    /**
     * Get the {@link SwerveDriveKinematics} associated with this swerve drive subsystem.
     * @return  The kinematics class.
     */
    public SwerveDriveKinematics getKinematics () {
        return kinematics;
    }
    
    /**
     * Drive the modules according to the given {@code speeds}.
     * @param speeds                    The {@link ChassisSpeeds} to drive the robot according to.
     */
    public void driveRobotRelative (ChassisSpeeds speeds) {
        SwerveModuleState[] desiredModuleStates = kinematics.toSwerveModuleStates(speeds);
        for (int i = 0; i < swerveModules.length; i ++) {
            // Drive the module to the desired state (optimizing) where the module will not turn if the speed is zero
            swerveModules[i].driveToStateOptimize(desiredModuleStates[i], false);
        }
    }
    
    /**
     * Drive all swerve modules to x-mode, where all modules face directly toward the center of the robot.
     * This allows the modules to lock into a configuration where the robot should be difficult to move.
     */
    public void driveXMode () {
        // Iterate through all swerve modules
        for (int i = 0; i < swerveModules.length; i ++) {
            // Get the angle the module's translation forms with the x-axis (i.e. the angle the
            // module can drive to in order to face directly toward the center of the robot)
            Rotation2d targetAngle = swerveModules[i].getTranslation().getAngle();
            
            // Drive the module to the target angle with zero speed
            swerveModules[i].driveToStateOptimize(new SwerveModuleState(0, targetAngle), true);
        }
    }
    
    /**
     * Get the maximum drive speed in meters per second of this swerve drive,
     * based on the maximum module drive speeds.
     * @return  The maximum drive speed, in meters per second.
     */
    public double getMaxDriveSpeedMetersPerSec () {
        double maxDriveSpeed = Double.POSITIVE_INFINITY;
        for (SwerveModuleBase module : swerveModules) {
            maxDriveSpeed = Math.min(maxDriveSpeed, module.getMaxDriveSpeedMetersPerSec());
        }
        
        return Math.max(maxDriveSpeed, 0);
    }
    
    /**
     * Immediately stop all swerve modules.
     */
    public void stop () {
        for (SwerveModuleBase module : swerveModules) {
            module.stop();
        }
    }
    
    /**
     * Get the {@link SwerveModulePosition}s of the swerve modules.
     * @return  An array with type {@code SwerveModulePosition[]} describing the current positions of the swerve modules.
     */
    public SwerveModulePosition[] getModulePositions () {
        SwerveModulePosition[] positions = new SwerveModulePosition[swerveModules.length];
        for (int i = 0; i < positions.length; i ++) {
            positions[i] = swerveModules[i].getPosition();
        }
        
        return positions;
    }
    
    /**
     * Reset the estimated robot pose. To correct with vision-estimated pose, use
     * {@link #addVisionPoseEstimate(Pose2d)} instead.
     * @param newPose           The new robot {@link Pose2d}.
     */
    public void resetPoseEstimation (Pose2d newPose) {
        poseEstimator.resetPosition(absoluteRotationSupplier.get(), getModulePositions(), newPose);
    }
    
    /**
     * Add a pose estimate based on vision data to use to correct in the pose estimator.
     * @param visionEstimate    The vision-estimated {@link Pose2d}.
     * @param time              Use {@link Timer#getFPGATimestamp()} to get the timestamp to use here. This should be the
     * timestamp when the pose estimate was taken.
     */
    public void addVisionPoseEstimate (Pose2d visionEstimate, double time) {
        poseEstimator.addVisionMeasurement(visionEstimate, 0);
    }
    
    /**
     * Add a pose estimate based on vision data to use to correct in the pose estimator. The
     * timestamp of the vision pose estimate is assumed to be the current timestamp.
     * @param visionEstimate    The vision-estimated {@link Pose2d}.
     */
    public void addVisionPoseEstimate (Pose2d visionEstimate) {
        poseEstimator.addVisionMeasurement(visionEstimate, Timer.getFPGATimestamp());
    }
    
    /**
     * Get the estimated robot pose. In order for this to be accurate,
     * {@link #periodicUpdate()} must be called periodically.
     * @return The estimated {@link Pose2d}.
     */
    public Pose2d getRobotPoseEstimate () {
        return poseEstimator.getEstimatedPosition();
    }
    
    /**
     * This should be called periodically.
     */
    public void periodicUpdate () {
        // Update the pose estimator
        poseEstimator.update(absoluteRotationSupplier.get(), getModulePositions());
    }
    
}
