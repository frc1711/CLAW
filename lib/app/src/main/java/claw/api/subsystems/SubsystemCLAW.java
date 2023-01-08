package claw.api.subsystems;

import claw.CLAWRobot;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class SubsystemCLAW extends SubsystemBase {
    
    public SubsystemCLAW () {
        super();
        CLAWRobot.getInstance().addSubsystem(this);
    }
    
}
