package claw.api.devices;

import edu.wpi.first.wpilibj.DigitalInput;

public class DigitalInputDevice extends Device<DigitalInput> {
    
    public DigitalInputDevice (String deviceName, int defaultId) {
        super(DigitalInput.class, deviceName, defaultId);
    }
    
    @Override
    public void initConfig (ConfigBuilder builder) {
        builder.addField("value", () -> {
            return "" + get().get();
        });
    }
    
    @Override
    protected DigitalInput initializeDevice (int id) {
        return new DigitalInput(id);
    }
    
    @Override
    protected void closeDevice (DigitalInput digitalInput) {
        digitalInput.close();
    }
    
}
