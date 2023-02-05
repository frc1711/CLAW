package claw.replay;

import claw.replay.action.RobotAction;
import edu.wpi.first.wpilibj2.command.CommandBase;

/**
 * An abstract class which supports autonomous replay--the replayable command should be able to
 * encode actions performed in teleop mode into a single {@link RobotAction} via {@link #getRecordedAction()}.
 */
public abstract class ReplayableCommand extends CommandBase {
    public abstract void startRecordingAction ();
    public abstract RobotAction getRecordedAction ();
}
