package claw.hardware;

import edu.wpi.first.wpilibj.DigitalInput;

/**
 * An implementation of {@link Device} for limit switches connected via DIO.
 */
public class LimitSwitchDevice extends Device<DigitalInput> {
    
    private final NormalState normalState;
    
    /**
     * Create a new {@link LinkSwitchDevice}.
     * @param deviceName    The unique device name.
     * @param normalState   The {@link NormalState} of the limit switch's circuit.
     */
    public LimitSwitchDevice (String deviceName, NormalState normalState) {
        super(deviceName, DigitalInput::new, DigitalInput::close);
        this.normalState = normalState;
    }
    
    /**
     * Check whether or not the limit switch is pressed.
     * @return  {@code true} if the limit switch is pressed.
     */
    public boolean isPressed () {
        return (normalState == NormalState.NORMALLY_OPEN) != get().get();
    }
    
    /**
     * An enum describing possible normal states of a limit switch's circuit.
     */
    public enum NormalState {
        /**
         * For a normally open limit switch, if the limit switch is not pressed, the circuit will be open,
         * and so a low signal will be received through DIO. Because of this, if the DIO is unplugged,
         * the input to the roboRIO will indicate that the limit switch is not pressed.
         */
        NORMALLY_OPEN,
        
        /**
         * For a normally closed limit switch, if the limit switch is not pressed, the circuit will be closed,
         * and so a high signal will be received through DIO. Because of this, if the DIO is unplugged,
         * the input to the roboRIO will indicate that the limit switch is pressed.
         */
        NORMALLY_CLOSED,
    }
    
}
