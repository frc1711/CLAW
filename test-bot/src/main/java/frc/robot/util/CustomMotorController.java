package frc.robot.util;

import edu.wpi.first.wpilibj.motorcontrol.MotorController;

public class CustomMotorController implements MotorController {
    
    public CustomMotorController (int id) { }
    
    @Override
    public void set (double speed) { }
    
    @Override
    public double get () { return 0; }
    
    @Override
    public void setInverted (boolean isInverted) { }
    
    @Override
    public boolean getInverted () { return false; }
    
    @Override
    public void disable () { }
    
    @Override
    public void stopMotor () { }
    
}
