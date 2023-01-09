package claw.api.devices;

import edu.wpi.first.wpilibj.motorcontrol.MotorController;

public class MotorControllerDevice <T extends MotorController> extends Device <T> {
    
    private final DeviceInitializer<T> initializer;
    private double configSetSpeed = 0;
    
    public MotorControllerDevice (Class<T> deviceClass, String deviceName, int defaultId, DeviceInitializer<T> initializer) {
        super(deviceClass, deviceName, defaultId);
        this.initializer = initializer;
    }
    
    @Override
    public void initConfig (ConfigBuilder builder) {
        builder.addDoubleMethod("set", (Double speed) -> {
            configSetSpeed = 0;
        });
        
        builder.addMethod("stop", () -> {
            configSetSpeed = 0;
        });
    }
    
    @Override
    public void configPeriodic () {
        get().set(configSetSpeed);
    }
    
    @Override
    public final T initializeDevice (int id) {
        T motor = initializer.initializeDevice(id);
        motor.stopMotor();
        return motor;
    }
    
    @Override
    protected void closeDevice (T device) {
        device.stopMotor();
    }
    
}
