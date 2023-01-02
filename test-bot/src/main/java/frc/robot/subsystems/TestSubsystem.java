package frc.robot.subsystems;

import claw.subsystems.SubsystemCLAW;
import edu.wpi.first.util.sendable.SendableBuilder;

public class TestSubsystem extends SubsystemCLAW {
    
    @Override
    public void initSendable (SendableBuilder builder) {
        builder.addStringProperty("subsystem key name", () -> "subsystem string value", str -> {});
    }
    
}
