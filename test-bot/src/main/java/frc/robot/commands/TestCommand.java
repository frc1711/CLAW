package frc.robot.commands;

import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.TestSubsystem;

public class TestCommand extends CommandBase {
    
    private final TestSubsystem subsystem;
    
    public TestCommand (TestSubsystem subsystem) {
        this.subsystem = subsystem;
        addRequirements(subsystem);
    }
    
    @Override
    public void initSendable (SendableBuilder builder) {
        builder.addStringProperty("command key name", () -> "command string value", str -> {});
    }
    
    @Override
    public void initialize () {
        
    }
    
    @Override
    public void execute () {
        
    }
    
    @Override
    public void end (boolean interrupted) {
        
    }
    
    @Override
    public boolean isFinished () {
        return false;
    }
    
}
