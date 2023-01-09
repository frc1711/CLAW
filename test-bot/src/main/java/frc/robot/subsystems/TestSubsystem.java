package frc.robot.subsystems;

import claw.api.subsystems.SubsystemCLAW;
import edu.wpi.first.util.sendable.SendableBuilder;
import frc.robot.util.CustomMotorController;

public class TestSubsystem extends SubsystemCLAW {
    
    private final CustomMotorController motor = new CustomMotorController(0);
    
    @Override
    public void initSendable (SendableBuilder builder) {
        builder.addStringProperty("subsystem key name", () -> "subsystem string value", str -> {});
    }
    
    public void set (double speed) {
        motor.set(speed);
    }
    
    public void stop () {
        motor.stopMotor();
    }
    
}
