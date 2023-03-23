package claw.hardware.swerve;

import claw.hardware.swerve.SwerveMotionConstraints.ChassisSpeedsFilter;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

public class SwerveTeleopController {
    
    private final SwerveDriveHandler swerveDrive;
    private final ChassisSpeedsFilter speedsFilter;
    private Rotation2d teleopZeroRotationOffset = new Rotation2d();
    
    public SwerveTeleopController (SwerveDriveHandler swerveDrive, SwerveMotionConstraints motionConstraints) {
        this.swerveDrive = swerveDrive;
        speedsFilter = motionConstraints.getSpeedsFilter();
    }
    
    public void driveRobotRelative (ChassisSpeeds desiredSpeeds) {
        swerveDrive.driveRobotRelative(speedsFilter.calculate(desiredSpeeds));
    }
    
    public void driveFieldRelative (ChassisSpeeds desiredSpeeds) {
        driveRobotRelative(ChassisSpeeds.fromFieldRelativeSpeeds(desiredSpeeds, getTeleopRotation()));
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
