package frc.robot.commands;

import claw.logs.CLAWLogger;
import claw.RobotErrorLog;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.TestSubsystem;

public class TestCommand extends CommandBase {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("commands.testcommand");
    private final TestSubsystem subsystem;
    
    public TestCommand (TestSubsystem subsystem) {
        this.subsystem = subsystem;
        addRequirements(subsystem);
        LOG.out("Constructing TestCommand");
    }
    
    @Override
    public void initialize () {
        LOG.out("Initializing TestCommand");
        RobotErrorLog.logWarning("This is a warning");
    }
    
    @Override
    public void execute () {
        
    }
    
    @Override
    public void end (boolean interrupted) {
        LOG.out("Ending TestCommand");
    }
    
    @Override
    public boolean isFinished () {
        return false;
    }
    
}
