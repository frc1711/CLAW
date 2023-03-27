package claw.hardware.swerve.auto;

import claw.math.Vector;
import edu.wpi.first.math.controller.HolonomicDriveController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.interpolation.Interpolatable;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.trajectory.Trajectory;

public interface SwerveAutoPath {
    
    public double getTotalTime ();
    public SwervePathState sampleState ();
    
    public default void test () {
        Trajectory.State state = new Trajectory.State(getTotalTime(), getTotalTime(), getTotalTime(), null, getTotalTime());
        new HolonomicDriveController(null, null, null).calculate(null, null, getTotalTime(), null);
        
        new SwervePathState(null, null).speedsFromController(null, null).vxMetersPerSecond += 4;
        
    }
    
    public record SwervePathState (Pose2d desiredRobotPose, Vector<N2> desiredVelocity) implements Interpolatable<SwervePathState> {
        
        public ChassisSpeeds speedsFromController (HolonomicDriveController controller, Pose2d currentPose) {
            return controller.calculate(
                currentPose,
                new Pose2d(
                    desiredRobotPose.getTranslation(),
                    Vector.getAngle(desiredVelocity)    // See note below  
                ),
                desiredVelocity.getMagnitude(),
                desiredRobotPose.getRotation()  // See note below
            );
            
            // Because WPILib trajectories are designed for non-holonomic drivetrains, the way HolonomicDriveControllers
            // follow trajectories is a little stitched together. The trajectory pose given to the controller must always
            // point in the direction the robot is moving in (again, non-holonimic), so the actual desired robot rotation
            // must be passed in separately
        }
        
        @Override
        public SwervePathState interpolate (SwervePathState endValue, double t) {
            return new SwervePathState(
                desiredRobotPose.interpolate(endValue.desiredRobotPose, t),
                desiredVelocity.interpolate(endValue.desiredVelocity, t)
            );
        }
        
    }
    
}
