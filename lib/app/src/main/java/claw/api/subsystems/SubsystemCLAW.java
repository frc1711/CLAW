package claw.api.subsystems;

import java.util.HashMap;
import java.util.Map;

import claw.CLAWRobot;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public abstract class SubsystemCLAW extends SubsystemBase {
    
    /**
     * A map of field prefixes onto functions
     */
    private final Map<String, ConfigurableFunction> functions = new HashMap<>();
    
    public SubsystemCLAW () {
        super();
        CLAWRobot.getInstance().addSubsystem(this);
    }
    
    protected void addFunction (String fieldPrefix, ConfigurableFunction function) {
        functions.put(fieldPrefix, function);
    }
    
    protected void addMotor (String fieldPrefix, MotorController motorController) {
        addFunction(fieldPrefix, new MotorControllerFunction(motorController));
    }
    
    protected void stopAllMotorControllers () {
        functions.values().forEach(func -> {
            if (func instanceof MotorControllerFunction)
                ((MotorControllerFunction)func).motorController.stopMotor();
        });
    }
    
    public abstract void stop ();
    
    @Override
    public final void initSendable (SendableBuilder builder) {
        functions.forEach((fieldPrefix, function) -> {
            function.addToSendable(fieldPrefix+"-", builder);
        });
    }
    
}
