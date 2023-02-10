package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.util.CustomMotorController;

public class TestSubsystem extends SubsystemBase {
    
    private final CustomMotorController motor = new CustomMotorController(0);
    
    public void set (double speed) {
        motor.set(speed);
    }
    
    public void stop () {
        motor.stopMotor();
    }
    
}
