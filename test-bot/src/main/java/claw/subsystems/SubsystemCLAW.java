package claw.subsystems;

import claw.api.RaptorsCLAW;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class SubsystemCLAW extends SubsystemBase {
    
    public SubsystemCLAW () {
        super();
        RaptorsCLAW.getInstance().addSubsystem(this);
    }
    
}
