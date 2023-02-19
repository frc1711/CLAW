package frc.robot.subsystems;

import claw.hardware.Device;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.util.CustomMotorController;
import frc.robot.util.TestDigitalInput;

public class TestSubsystem extends SubsystemBase {
    
    private final Device<CustomMotorController> armMotor = new Device<>(
        "CAN.MOTOR_CONTROLLER.TEST_SYSTEM.ARM",
        id -> new CustomMotorController(id),
        motor -> motor.stopMotor()
    );
    
    private final Device<TestDigitalInput> armUpperLimitSwitch = new Device<>(
        "DIO.LIMIT_SWITCH.TEST_SYSTEM.UPPER_ARM",
        id -> new TestDigitalInput(id),
        digitalInput -> digitalInput.close()
    );
    
    private final Device<TestDigitalInput> armLowerLimitSwitch = new Device<>(
        "DIO.LIMIT_SWITCH.TEST_SYSTEM.LOWER_ARM",
        id -> new TestDigitalInput(id),
        digitalInput -> digitalInput.close()
    );
    
    public void set (double speed) {
        if (armUpperLimitSwitch.get().get())
            armMotor.get().stopMotor();
        else
            armMotor.get().set(speed);
    }
    
    public void stop () {
        armMotor.get().stopMotor();
    }
    
}
