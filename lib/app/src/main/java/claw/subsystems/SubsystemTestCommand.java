package claw.subsystems;

import java.util.Set;

import claw.LiveValues;
import claw.rct.network.low.ConsoleManager;
import claw.subsystems.SubsystemTest.TestSection;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;

public class SubsystemTestCommand implements TestSection {
    
    private final ExecuteCycle executeCycle;
    
    public SubsystemTestCommand (ExecuteCycle executeCycle) {
        this.executeCycle = executeCycle;
    }
    
    @Override
    public void run (CLAWSubsystem subsystem, String testName, ConsoleManager console) {
        
        // Create a new test-command wrapper
        TestCommandWrapper command = new TestCommandWrapper(subsystem, testName);
        
        // Schedule the command
        // TODO: Fix ConcurrentModificationException by making this synchronized to the main thread
        CommandScheduler.getInstance().schedule(command);
        
        // Continue updating the livevalues until the command has been descheduled
        while (!command.hasFinished) {
            command.values.update(console);
        }
        
    }
    
    @FunctionalInterface
    public interface ExecuteCycle {
        
        /**
         * Executes one periodic cycle of the {@link SubsystemTestCommand}.
         * @param debugValues   The debug {@link LiveValues} to use for conveniently displaying information to the console.
         * @return              Whether or not the command should finish executing.
         */
        public boolean execute (LiveValues debugValues);
        
    }
    
    private class TestCommandWrapper implements Command {
        
        private final LiveValues values = new LiveValues();
        private final CLAWSubsystem subsystem;
        private final String testName;
        private boolean commandShouldFinish = false;
        
        private boolean hasFinished = false;
        
        public TestCommandWrapper (CLAWSubsystem subsystem, String testName) {
            this.subsystem = subsystem;
            this.testName = testName;
        }
        
        @Override
        public String getName () {
            return "SubsystemTestCommand<"+subsystem.getName()+">(\""+testName+"\")";
        }
        
        @Override
        public Set<Subsystem> getRequirements () {
            return Set.of(subsystem);
        }
        
        @Override
        public void initialize () {
            subsystem.stop();
        }
        
        @Override
        public void execute () {
            commandShouldFinish = executeCycle.execute(values);
        }
        
        @Override
        public void end (boolean interrupted) {
            subsystem.stop();
            hasFinished = true;
        }
        
        @Override
        public boolean isFinished () {
            return commandShouldFinish;
        }
        
        @Override
        public InterruptionBehavior getInterruptionBehavior () {
            return InterruptionBehavior.kCancelIncoming;
        }
        
    }
    
}
