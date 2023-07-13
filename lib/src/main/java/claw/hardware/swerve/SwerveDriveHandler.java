package claw.hardware.swerve;

import java.util.function.Supplier;

import claw.hardware.swerve.tests.ModuleDriveEncoderTest;
import claw.hardware.swerve.tests.ModuleRotationEncoderTest;
import claw.hardware.swerve.tests.ModuleUniformCommandTest;
import claw.subsystems.SubsystemTest;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.Timer;

/**
 * A class which handles swerve drive kinematics and odometry for easy swerve control and implementation.
 */
public class SwerveDriveHandler implements Sendable {
    
    private final SwerveDriveKinematics kinematics;
    private final Supplier<Rotation2d> absoluteRotationSupplier;
    private final SwerveModuleBase[] swerveModules;
    private final Translation2d[] moduleTranslations;
    private final SwerveDrivePoseEstimator poseEstimator;
    
    private Rotation2d fieldRelRotationOffset = new Rotation2d();
    
    /**
     * Create a new {@link SwerveDriveHandler}. Note that {@link #periodicUpdate()} must be called periodically.
     * @param initialRobotPose          The robot's initial {@link Pose2d}.
     * @param absoluteRotationSupplier  A {@link Supplier} providing {@link Rotation2d} describing the robot's rotation.
     * <b>This should never reset, and its basis should never change.</b> Counter-clockwise must be positive.
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
            getRobotRotation(),
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
     * Drive the modules according to the given robot-relative {@code speeds}.
     * @param speeds    The {@link ChassisSpeeds} to drive the robot according to.
     */
    public void drive (ChassisSpeeds speeds) {
        // Get module speeds and desaturate so they don't go beyond the capabilities of any of the modules
        SwerveModuleState[] desiredModuleStates = kinematics.toSwerveModuleStates(speeds);
        SwerveDriveKinematics.desaturateWheelSpeeds(desiredModuleStates, getMaxDriveSpeedMetersPerSec());
        
        // Drive each module
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
     * Get the {@link SwerveModuleState}s of the swerve modules.
     * @return  An array with type {@code SwerveModuleState[]} describing the current states of the swerve modules.
     */
    public SwerveModuleState[] getModuleStates () {
        SwerveModuleState[] states = new SwerveModuleState[swerveModules.length];
        for (int i = 0; i < states.length; i ++) {
            states[i] = swerveModules[i].getState();
        }
        
        return states;
    }
    
    /**
     * Get the array containing the {@link SwerveModuleBase}s used by this swerve drive.
     * @return  The swerve modules.
     */
    public SwerveModuleBase[] getModules () {
        return swerveModules.clone();
    }
    
    /**
     * Get robot-relative speeds measured from the swerve modules.
     * @return The measured {@link ChassisSpeeds}.
     */
    public ChassisSpeeds getRobotSpeed () {
        return kinematics.toChassisSpeeds(getModuleStates());
    }
    
    /**
     * Gets the robot's rotation on the field (CCW+). This may only be reset with {@link #resetPose(Pose2d)}.
     * This rotation will necessarily match the robot's estimated pose, and is the ultimate source of truth
     * as to the direction the robot is facing on the field.
     * @return The robot's field rotation.
     */
    public Rotation2d getRobotRotation () {
        return absoluteRotationSupplier.get().plus(fieldRelRotationOffset);
    }
    
    /**
     * Reset the estimated robot pose, along with {@link #getRobotRotation()}. To correct with vision-estimated
     * pose, use {@link #addVisionPoseEstimate(Pose2d)} instead. Use this method with caution, as systems relying
     * on the robot's rotation will have to adjust.
     * @param newPose           The new robot {@link Pose2d}.
     */
    public void resetPose (Pose2d newPose) {
        // Reset the fieldRelRotationOffset so getRobotRotation() will read the new pose's offset
        fieldRelRotationOffset = newPose.getRotation().minus(absoluteRotationSupplier.get());
        
        // Reset the pose estimator according to the new robot rotation and pose
        poseEstimator.resetPosition(getRobotRotation(), getModulePositions(), newPose);
    }
    
    /**
     * Convert field-relative {@link ChassisSpeeds} to robot-relative speeds, according to the current
     * robot rotation as measured by {@link #getRobotRotation()}.
     * @param fieldRelSpeeds    Field-relative speeds.
     * @return                  Robot-relative speeds.
     */
    public ChassisSpeeds toRobotRelativeSpeeds (ChassisSpeeds fieldRelSpeeds) {
        return ChassisSpeeds.fromFieldRelativeSpeeds(fieldRelSpeeds, getRobotRotation());
    }
    
    /**
     * Convert robot-relative {@link ChassisSpeeds} to field-relative speeds, according to the current
     * robot rotation as measured by {@link #getRobotRotation()}.
     * @param robotRelSpeeds    Robot-relative speeds.
     * @return                  Field-relative speeds.
     */
    public ChassisSpeeds toFieldRelativeSpeeds (ChassisSpeeds robotRelSpeeds) {
        return ChassisSpeeds.fromFieldRelativeSpeeds(robotRelSpeeds, getRobotRotation().unaryMinus());
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
        poseEstimator.update(getRobotRotation(), getModulePositions());
    }
    
    /**
     * Generate an array of {@link SubsystemTest}s which can be added to a {@link CLAWSubsystem}
     * to test and tune the aspects of a {@link SwerveDriveHandler} and its {@link SwerveModuleBase}s.
     */
    public SubsystemTest[] generateSubsystemTests () {
        return new SubsystemTest[] {
            new ModuleRotationEncoderTest(this),
            new ModuleDriveEncoderTest(this),
            new ModuleUniformCommandTest(this)
        };
    }
    
    @Override
    public void initSendable (SendableBuilder builder) {
        builder.addDoubleProperty("Field Robot Rotation (deg)", () -> getRobotRotation().getDegrees(), null);
    }
    
}
