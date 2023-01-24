package frc.robot.commands;

import claw.CLAWLogger;
import claw.LiveUnit;
import claw.UnitBuilder;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.TestSubsystem;

public class TestCommand extends CommandBase {
    
    private static final LiveUnit UNIT = new UnitBuilder().withName("TestCommand");
    
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
        subsystem.stop();
        
        UNIT.put("running", true);
    }
    
    @Override
    public void execute () {
        subsystem.set(0.4);
    }
    
    @Override
    public void end (boolean interrupted) {
        LOG.out("Ending TestCommand");
        subsystem.stop();
        
        UNIT.put("running", false);
    }
    
    @Override
    public boolean isFinished () {
        return false;
    }
    
}
