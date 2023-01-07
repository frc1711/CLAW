package frc.robot.subsystems;

import claw.devices.MotorControllerDevice;
import claw.subsystems.SubsystemCLAW;
import edu.wpi.first.util.sendable.SendableBuilder;
import frc.robot.util.CustomMotorController;

public class TestSubsystem extends SubsystemCLAW {
    
    private final MotorControllerDevice<CustomMotorController> device =
        new MotorControllerDevice<CustomMotorController>("MotorControllerDevice", CustomMotorController::new, 0);
    
    @Override
    public void initSendable (SendableBuilder builder) {
        builder.addStringProperty("subsystem key name", () -> "subsystem string value", str -> {});
    }
    
    public void set (double speed) {
        device.get().set(speed);
    }
    
    public void stop () {
        device.get().stopMotor();
    }
    
}
