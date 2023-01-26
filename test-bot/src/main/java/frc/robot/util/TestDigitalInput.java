package frc.robot.util;

import edu.wpi.first.wpilibj.DigitalInput;

public class TestDigitalInput extends DigitalInput {
    
    private boolean lastValue = false;
    
    public TestDigitalInput () {
        super(0);
    }
    
    @Override
    public boolean get () {
        if (Math.random() > 0.95) lastValue = !lastValue;
        return lastValue;
    }
    
}
