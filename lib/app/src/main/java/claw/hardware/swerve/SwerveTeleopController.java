package claw.hardware.swerve;

import claw.hardware.swerve.auto.SwerveMotionConstraints;
import claw.hardware.swerve.auto.ChassisSpeedsFilter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

/**
 * A teleop controller for {@link SwerveDriveHandler} which applies motion constraints
 * and has additional functionality for field-relative driving.
 */
public class SwerveTeleopController {
    
    private final SwerveDriveHandler swerveDrive;
    private final ChassisSpeedsFilter speedsFilter;
    private Rotation2d teleopZeroRotationOffset = new Rotation2d();
    
    /**
     * Create a new {@link SwerveTeleopController} given a swerve drive and a set of motion constraints.
     * @param swerveDrive       The {@link SwerveDriveHandler} to use for driving.
     * @param motionConstraints The {@link SwerveMotionConstraints} describing acceleration and velocity
     * limits.
     */
    public SwerveTeleopController (SwerveDriveHandler swerveDrive, SwerveMotionConstraints motionConstraints) {
        this.swerveDrive = swerveDrive;
        speedsFilter = new ChassisSpeedsFilter(swerveDrive, motionConstraints, new ChassisSpeeds());
    }
    
    /**
     * Perform robot-relative driving.
     * @param desiredSpeeds The robot-relative {@link ChassisSpeeds} which swerve will attempt
     * to drive to, after applying motion constraints.
     */
    public void driveRobotRelative (ChassisSpeeds desiredSpeeds) {
        swerveDrive.drive(speedsFilter.calculate(desiredSpeeds));
    }
    
    /**
     * Perform robot-relative driving, with an offset rotation. This is useful for allowing the robot to drive
     * relative to a camera on the robot which isn't pointed forwards.
     * @param desiredSpeeds     The robot-relative {@link ChassisSpeeds} which swerve will attempt
     * to drive to, after applying motion constraints.
     * @param rotationOffset    The offset robot-relative rotation which controls the relative direction of
     * the inputted speeds. For example, if {@code rotationOffset} is equivalent to pi/2 radians, the chassis
     * speeds will be relative to the left side of the robot.
     */
    public void driveRobotRelative (ChassisSpeeds desiredSpeeds, Rotation2d rotationOffset) {
        driveRobotRelative(ChassisSpeeds.fromFieldRelativeSpeeds(
            desiredSpeeds,
            rotationOffset.unaryMinus()
        ));
    }
    
    /**
     * Perform field-relative driving.
     * @param desiredSpeeds The field-relative {@link ChassisSpeeds} which swerve will attempt
     * to drive to, after applying motion constraints.
     */
    public void driveFieldRelative (ChassisSpeeds desiredSpeeds) {
        driveRobotRelative(ChassisSpeeds.fromFieldRelativeSpeeds(desiredSpeeds, getTeleopRotation()));
    }
    
    /**
     * Drive swerve to x-mode, where all modules face inward which can stop robot movement.
     */
    public void driveXMode () {
        swerveDrive.driveXMode();
        speedsFilter.reset();
    }
    
    /**
     * Immediately stop the swerve drive.
     */
    public void stop () {
        swerveDrive.stop();
        speedsFilter.reset();
    }
    
    /**
     * Get the robot-relative {@link ChassisSpeeds}.
     * @return  Measured robot chassis speeds.
     */
    public ChassisSpeeds getRobotSpeed () {
        return swerveDrive.getRobotSpeed();
    }
    
    /**
     * Get the "teleop" rotation of the robot, which allows field-relative drive control to use
     * a different basis of field relative rotation than what is used by the {@link SwerveDriveHandler}.
     * This rotation is what determines the direction the robot will drive in for {@link #driveFieldRelative(ChassisSpeeds)}.
     * @return  The rotation of the robot, as is used for field-relative driving through this controller.
     * @see {@link #setCurrentTeleopRotation(Rotation2d)}
     */
    public Rotation2d getTeleopRotation () {
        // Rt = Ra - Rz
        return swerveDrive.getAbsoluteRobotRotation().minus(teleopZeroRotationOffset);
    }
    
    /**
     * Set the current "teleop" rotation of the robot, which allows field-relative drive control
     * to use a different basis of field relative rotation than what is used by the {@link SwerveDriveHandler}.
     * This rotation is what determines the direction the robot will drive in for {@link #driveFieldRelative(ChassisSpeeds)}.
     * @param teleopRotation    The value which should be read by {@link #getTeleopRotation()} at this physical rotation
     * of the robot (as read by {@link SwerveDriveHandler#getAbsoluteRobotRotation()}).
     */
    public void setCurrentTeleopRotation (Rotation2d teleopRotation) {
        // Rz = Ra - Rt
        teleopZeroRotationOffset = swerveDrive.getAbsoluteRobotRotation().minus(teleopRotation);
    }
    
}
