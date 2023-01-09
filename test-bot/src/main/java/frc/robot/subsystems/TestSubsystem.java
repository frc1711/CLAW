package frc.robot.subsystems;

import claw.api.devices.MotorControllerDevice;
import claw.api.subsystems.SubsystemCLAW;
import edu.wpi.first.util.sendable.SendableBuilder;
import frc.robot.util.CustomMotorController;

public class TestSubsystem extends SubsystemCLAW {
    
    private final MotorControllerDevice<CustomMotorController> device =
        new MotorControllerDevice<CustomMotorController>(CustomMotorController.class, "MotorControllerDevice", 0, CustomMotorController::new);
    
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
