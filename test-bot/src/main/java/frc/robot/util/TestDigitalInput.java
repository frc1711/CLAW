package frc.robot.util;

import edu.wpi.first.wpilibj.DigitalInput;

public class TestDigitalInput extends DigitalInput {
    
    private boolean lastValue = false;
    
    public TestDigitalInput (int id) {
        super(id);
    }
    
    @Override
    public boolean get () {
        if (Math.random() > 0.98) lastValue = !lastValue;
        return lastValue;
    }
    
}
