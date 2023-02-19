package claw.replay;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

/**
 * A helper class to be used by a {@link RobotActionRecorder} to replay a 
 */
public class RobotActionSequence implements RobotActionRecord {
    
    public final RobotActionRecord[] actions;
    
    public RobotActionSequence (RobotActionRecord[] actions) {
        this.actions = actions;
    }
    
    public Command toReplayCommand () {
        Command[] commands = new Command[actions.length];
        for (int i = 0; i < commands.length; i ++)
            commands[i] = actions[i].toReplayCommand();
        return new SequentialCommandGroup(commands);
    }
    
}
