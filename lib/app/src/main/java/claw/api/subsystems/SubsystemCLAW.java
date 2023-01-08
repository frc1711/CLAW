package claw.api.subsystems;

import claw.CLAWRuntime;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class SubsystemCLAW extends SubsystemBase {
    
    public SubsystemCLAW () {
        super();
        CLAWRuntime.getInstance().addSubsystem(this);
    }
    
}
