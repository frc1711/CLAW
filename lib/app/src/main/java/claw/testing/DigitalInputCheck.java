package claw.testing;

import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj.DigitalInput;

public class DigitalInputCheck extends SystemsCheck {
    
    private final DigitalInput digitalInput;
    private final String explanation;
    
    public DigitalInputCheck (String systemName, String explanation, DigitalInput digitalInput) {
        super(systemName);
        this.explanation = explanation;
        this.digitalInput = digitalInput;
    }
    
    @Override
    public void run (ConsoleManager console) {
        console.println("Digital input on DIO channel " + digitalInput.getChannel());
        console.println(explanation+"\n\n");
        
        while (!console.hasInputReady()) {
            console.moveUp(1);
            console.clearLine();
            console.println("Reading value: " + digitalInput.get());
        }
    }
    
    @Override
    public boolean isActuatingCheck () {
        return false;
    }
    
}
