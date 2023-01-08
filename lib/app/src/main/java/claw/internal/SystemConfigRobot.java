package claw.internal;

import claw.api.devices.Device;
import edu.wpi.first.wpilibj.TimedRobot;

public class SystemConfigRobot extends TimedRobot {
    
    private final Registry<Device<Object>> deviceRegistry;
    
    public SystemConfigRobot (Registry<Device<Object>> deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }
    
    @Override
    public void robotInit () { }
    
    @Override
    public void robotPeriodic() { }
    
    @Override
    public void disabledPeriodic () { }
    
    @Override
    public void autonomousInit () {
        enabledInit();
    }
    
    @Override
    public void autonomousPeriodic () {
        enabledPeriodic();}
    
    @Override
    public void teleopInit () {
        enabledInit();
    }
    
    @Override
    public void teleopPeriodic () {
        enabledPeriodic();
    }
    
    @Override
    public void testInit () {
        enabledInit();
    }
    
    @Override
    public void testPeriodic () {
        enabledPeriodic();
    }
    
    @Override
    public void simulationInit () { }
    
    @Override
    public void simulationPeriodic () { }
    
    
    
    
    
    
    private void enabledInit () {
        
    }
    
    private void enabledPeriodic () {
        
    }
    
    @Override
    public void disabledInit () {
        // TODO: Disable all device controllers
    }
    
}
