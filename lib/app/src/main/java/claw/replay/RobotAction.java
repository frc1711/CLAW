package claw.replay;

import java.io.Serializable;

import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * A single action the robot can take, which supports both conversion into a replay command with {@link #toReplayCommand()} and
 * serialization so the action can be saved to the roboRIO.
 */
public interface RobotAction extends Serializable {
    
    /**
     * Convert this action into a command which will replay the encoded robot action.
     * @return An autonomous command (requiring no driverstation input) which replays the action.  
     */
    public Command toReplayCommand ();
    
    /**
     * A method which can be overridden to allow for inspecting the details of this action via the
     * robot control terminal.
     * @param console   The {@link ConsoleManager} allowing for RCT console control.
     */
    public default void inspect (ConsoleManager console) {
        console.println(
            "This robot action does not yet support terminal inspection.\n" +
            "Override this action's inspect method."
        );
    }
    
}
