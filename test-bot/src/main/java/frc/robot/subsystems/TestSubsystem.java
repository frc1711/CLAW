package frc.robot.subsystems;

import claw.api.liveunits.LiveUnit;
import claw.api.liveunits.UnitBuilder;
import claw.api.subsystems.SubsystemCLAW;
import frc.robot.util.CustomMotorController;

public class TestSubsystem extends SubsystemCLAW {
    
    private static final LiveUnit UNIT = new UnitBuilder().withName("TestSubsystem");
    
    private final CustomMotorController motor = new CustomMotorController(0);
    
    private int count = 0;
    
    public void set (double speed) {
        motor.set(speed);
        UNIT.put("speed", speed);
    }
    
    public void stop () {
        motor.stopMotor();
        UNIT.put("speed", 0);
        
        count ++;
        UNIT.put("count", count);
    }
    
}
