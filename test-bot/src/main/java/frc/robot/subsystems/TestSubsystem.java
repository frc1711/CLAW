package frc.robot.subsystems;

import claw.CLAWRobot;
import claw.LiveUnit;
import claw.UnitBuilder;
import claw.testing.BooleanInputCheck;
import claw.testing.RobotSystemsChecks;
import claw.testing.SystemsCheck;
import edu.wpi.first.wpilibj.DigitalInput;
import claw.SubsystemCLAW;
import frc.robot.util.CustomMotorController;
import frc.robot.util.TestDigitalInput;

public class TestSubsystem extends SubsystemCLAW {
    
    private static final LiveUnit UNIT = new UnitBuilder().withName("TestSubsystem");
    
    private final CustomMotorController motor = new CustomMotorController(0);
    private final DigitalInput dInput = new TestDigitalInput();
    
    private int count = 0;
    
    public TestSubsystem () {
        SystemsCheck[] checksArray = new SystemsCheck[]{
            new BooleanInputCheck(dInput::get, "Follow the directions to toggle the thing", "pressed", "not pressed")
        };
        
        RobotSystemsChecks checks = new RobotSystemsChecks(checksArray);
        checks.bindToInterpreter(CLAWRobot.getExtensibleCommandInterpreter(), "syscheck");
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
