package frc.robot.commands;

import claw.CLAWRuntime;
import claw.rct.network.messages.StreamDataMessage.StreamData;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.TestSubsystem;

public class TestCommand extends CommandBase {
    
    private final TestSubsystem subsystem;
    
    public TestCommand (TestSubsystem subsystem) {
        this.subsystem = subsystem;
        addRequirements(subsystem);
        CLAWRuntime.getInstance().testSendStreamData(new StreamData("TestCommandStream", "Constructing TestCommand"));
    }
    
    @Override
    public void initSendable (SendableBuilder builder) {
        builder.addStringProperty("command key name", () -> "command string value", str -> {});
    }
    
    @Override
    public void initialize () {
        CLAWRuntime.getInstance().testSendStreamData(new StreamData("TestCommandStream", "Initializing TestCommand"));
    }
    
    @Override
    public void execute () {
        CLAWRuntime.getInstance().testSendStreamData(new StreamData("TestCommandStream", "Executing TestCommand"));
    }
    
    @Override
    public void end (boolean interrupted) {
        CLAWRuntime.getInstance().testSendStreamData(new StreamData("TestCommandStream", "Ending TestCommand"));
    }
    
    @Override
    public boolean isFinished () {
        return false;
    }
    
}
