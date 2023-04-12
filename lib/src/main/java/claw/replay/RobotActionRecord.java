package claw.replay;

import java.io.Serializable;

import edu.wpi.first.wpilibj2.command.Command;

/**
 * A single action the robot can take, which supports both conversion into a replay command with {@link #toReplayCommand()} and
 * serialization so the action can be saved to the roboRIO.
 */
public interface RobotActionRecord extends Serializable {
    
    /**
     * Convert this action into a command which will replay the encoded robot action.
     * @return An autonomous command (requiring no driverstation input) which replays the action.  
     */
    public Command toReplayCommand ();
    
}
