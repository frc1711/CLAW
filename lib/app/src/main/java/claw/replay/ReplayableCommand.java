package claw.replay;

import edu.wpi.first.wpilibj2.command.CommandBase;

/**
 * An abstract class which supports autonomous replay--the replayable command should be able to
 * encode actions performed in teleop mode into a single {@link RobotAction} via {@link #endAndRecord(boolean)}.
 * Whenever this command ends, its {@code RobotAction} will be automatically stored 
 */
public abstract class ReplayableCommand extends CommandBase {
    
    @Override
    public final void end (boolean interrupted) {
        RobotActionRecorder.addAction(endAndRecord(interrupted));
    }
    
    public abstract RobotAction endAndRecord (boolean interrupted);
    
}
