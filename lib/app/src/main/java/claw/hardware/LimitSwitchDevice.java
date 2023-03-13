package claw.hardware;

import edu.wpi.first.wpilibj.DigitalInput;

/**
 * A wrapper around {@link DigitalInput} for reading a value from a limit switch.
 */
public class LimitSwitchDevice implements AutoCloseable {
    
    private final DigitalInput limitSwitchInput;
    private final NormalState normalState;
    
    /**
     * Create a new {@link LinkSwitchDevice}.
     * @param normalState   The {@link NormalState} of the limit switch's circuit.
     */
    public LimitSwitchDevice (DigitalInput limitSwitchInput, NormalState normalState) {
        this.limitSwitchInput = limitSwitchInput;
        this.normalState = normalState;
    }
    
    /**
     * Check whether or not the limit switch is pressed.
     * @return  {@code true} if the limit switch is pressed.
     */
    public boolean isPressed () {
        return (normalState == NormalState.NORMALLY_OPEN) != limitSwitchInput.get();
    }
    
    /**
     * An enum describing possible normal states of a limit switch's circuit.
     */
    public enum NormalState {
        /**
         * For a normally open limit switch, if the limit switch is not pressed, the circuit will be open,
         * and so a high signal (5V) will be received through DIO. Because of this, if the DIO is unplugged,
         * the input to the roboRIO will indicate that the limit switch is pressed.
         */
        NORMALLY_OPEN,
        
        /**
         * For a normally closed limit switch, if the limit switch is not pressed, the circuit will be closed,
         * and so a low signal (0V) will be received through DIO. Because of this, if the DIO is unplugged,
         * the input to the roboRIO will indicate that the limit switch is not pressed.
         */
        NORMALLY_CLOSED,
    }
    
    @Override
    public void close () {
        limitSwitchInput.close();
    }
    
}
