package claw.api.devices;

import edu.wpi.first.wpilibj.motorcontrol.MotorController;

public class MotorControllerDevice <T extends MotorController> extends Device <T> {
    
    private final DeviceInitializer<T> initializer;
    
    public MotorControllerDevice (String deviceName, DeviceInitializer<T> initializer, int defaultId) {
        super(deviceName, defaultId);
        this.initializer = initializer;
    }
    
    @Override
    protected T initializeDevice (int id) {
        T motor = initializer.initializeDevice(id);
        motor.stopMotor();
        return motor;
    }
    
    @Override
    protected void closeDevice (T device) {
        device.stopMotor();
    }
    
}
