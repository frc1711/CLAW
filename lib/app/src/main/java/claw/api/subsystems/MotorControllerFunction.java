package claw.api.subsystems;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;

class MotorControllerFunction implements ConfigurableFunction {
    
    public final MotorController motorController;
    
    public MotorControllerFunction (MotorController motorController) {
        this.motorController = motorController;
    }
    
    public void addToSendable (String prefix, SendableBuilder builder) {
        builder.addDoubleProperty(prefix+"speed", motorController::get, null);
    }
    
}
