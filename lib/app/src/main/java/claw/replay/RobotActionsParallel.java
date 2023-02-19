package claw.replay;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

public class RobotActionsParallel implements RobotActionRecord {
    
    public final RobotActionRecord[] actions;
    
    public RobotActionsParallel (RobotActionRecord[] actions) {
        this.actions = actions;
    }
    
    public Command toReplayCommand () {
        Command[] commands = new Command[actions.length];
        for (int i = 0; i < commands.length; i ++)
            commands[i] = actions[i].toReplayCommand();
        return new ParallelCommandGroup(commands);
    }
    
}

