package claw.api.devices;

import edu.wpi.first.wpilibj.DigitalInput;

public class DigitalInputDevice extends Device<DigitalInput> {
    
    public DigitalInputDevice (String deviceName, int defaultId) {
        super(deviceName, defaultId);
    }
    
    @Override
    protected DigitalInput initializeDevice (int id) {
        return new DigitalInput(id);
    }
    
    @Override
    protected void closeDevice (DigitalInput device) {
        device.close();
    }
    
}
