package claw.devices;

import claw.logs.LogHandler;
import claw.logs.RCTLog;

public abstract class Device <T> {
    
    private static final RCTLog LOG = LogHandler.getInstance().getSysLog("Devices");
    
    private final String deviceName;
    
    private int deviceId;
    private T device;
    
    public Device (String deviceName, int defaultId) {
        this.deviceName = deviceName;
        this.deviceId = defaultId;
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
        if (device != null)
            closeDevice(device);
        LOG.out("Resetting device '"+deviceName+"'");
        device = initializeDevice(id);
    }
    
    protected abstract T initializeDevice (int deviceId);
    protected abstract void closeDevice (T device);
    
    public static interface DeviceInitializer <T> {
        public T initializeDevice (int deviceId);
    }
    
    public static interface DeviceCloser <T> {
        public void closeDevice (T device);
    }
    
}
