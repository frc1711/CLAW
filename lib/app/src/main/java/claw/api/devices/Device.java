package claw.api.devices;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import claw.CLAWRobot;
import claw.api.CLAWLogger;
import claw.api.devices.ConfigBuilder.BadMethodCall;
import claw.api.devices.ConfigBuilder.ConfigMethod;

public abstract class Device <T> {
    
    // TODO: Access device log statically
    private static final CLAWLogger LOG = CLAWLogger.getSysLog("Devices");
    
    private final ConfigBuilder configBuilder = new ConfigBuilder();
    
    private final Class<T> deviceClass;
    private final String deviceName;
    
    private int deviceId;
    private T device;
    
    @SuppressWarnings("unchecked")
    public Device (Class<T> deviceClass, String deviceName, int defaultId) {
        this.deviceClass = deviceClass;
        this.deviceName = deviceName;
        this.deviceId = defaultId;
        
        CLAWRobot.getInstance().addDevice((Device<?>)this);
        
        initConfig(configBuilder);
    }
    
    public String getName () {
        return deviceName;
    }
    
    public T get () {
        if (device == null)
            reset(deviceId);
        return device;
    }
    
    public void reset (int id) {
        LOG.out("Resetting device '"+deviceName+"'");
        
        try {
            if (device != null)
                closeDevice(device);
        } catch (Exception e) {
            // TODO: Access to system logs outside CLAWLogger
            // TODO: Log the exception thrown here
        }
        
        device = initializeDevice(id);
        deviceId = id;
    }
    
    public void callConfigMethod (String line) throws BadMethodCall {
        configBuilder.callMethod(line);
    }
    
    public Set<String> getFields () {
        return configBuilder.getFields();
    }
    
    public Set<String> getMethods () {
        return configBuilder.getMethods();
    }
    
    /**
     * {@code null} if the field does not exist
     * @param field
     * @return
     */
    public String readField (String field) {
        return configBuilder.readField(field);
    }
    
    public void configPeriodic () { }
    
    protected void initConfig (ConfigBuilder builder) { }
    
    protected abstract T initializeDevice (int id);
    protected abstract void closeDevice (T device);
    
}
