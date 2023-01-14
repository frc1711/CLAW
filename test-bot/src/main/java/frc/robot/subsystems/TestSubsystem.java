package frc.robot.subsystems;

import claw.api.subsystems.SubsystemCLAW;
import frc.robot.util.CustomMotorController;

public class TestSubsystem extends SubsystemCLAW {
    
    private final CustomMotorController motor = new CustomMotorController(0);
    
    public void set (double speed) {
        motor.set(speed);
    }
    
    public void stop () {
        motor.stopMotor();
    }
    
}
