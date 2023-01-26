package frc.robot.subsystems;

import claw.LiveUnit;
import claw.UnitBuilder;
import claw.testing.DigitalInputCheck;
import claw.testing.SystemsCheck;
import claw.SubsystemCLAW;
import frc.robot.util.CustomMotorController;
import frc.robot.util.TestDigitalInput;

public class TestSubsystem extends SubsystemCLAW {
    
    private static final LiveUnit UNIT = new UnitBuilder().withName("TestSubsystem");
    
    private final CustomMotorController motor = new CustomMotorController(0);
    
    private int count = 0;
    
    public TestSubsystem () {
        SystemsCheck.addSystemsCheck(new DigitalInputCheck(getName(), "true means true, false means false", new TestDigitalInput()));
    }
    
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
