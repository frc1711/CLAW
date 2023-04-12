package claw.subsystems;

import java.util.Optional;

import claw.actions.Action;
import claw.actions.compositions.SubsystemTestComposer;
import claw.actions.compositions.SubsystemTestCompositionContext;
import claw.actions.compositions.Context.Operation;
import claw.rct.base.console.ConsoleManager;
import claw.rct.base.console.ConsoleManager.TerminalKilledException;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * Represents a simple test bound to a subsystem which can be run through the Robot Control Terminal.
 * @see CLAWSubsystem#addTests(SubsystemTest...)
 */
public class SubsystemTest {
    
    private final String name, description;
    private final TestCommandSupplier testCommandSupplier;
    
    /**
     * Create a new {@link SubsystemTest}. A subsystem test should only ever control the one subsystem to which it belongs.
     * Never add a subsystem test to a subsystem which it does not belong to.
     * @param name                  A name to identify the particular test by.
     * @param description           A description of what the test does and how it can be used.
     * @param testCommandSupplier   The {@link TestCommandSupplier} which will provide a test command.
     */
    public SubsystemTest (String name, String description, TestCommandSupplier testCommandSupplier) {
        this.name = name;
        this.description = description;
        this.testCommandSupplier = testCommandSupplier;
    }
    
    /**
     * Get the {@link SubsystemTest}'s name.
     * @return  The name of this test.
     */
    public String getName () {
        return name;
    }
    
    private static boolean getYesNo (ConsoleManager console, String prompt) throws TerminalKilledException {
        Optional<Boolean> answer = Optional.empty();
        
        console.println("");
        
        while (answer.isEmpty()) {
            console.moveUp(1);
            console.clearLine();
            console.print(prompt + "(yes | no) ");
            
            String input = console.readInputLine().strip().toUpperCase();
            
            answer = input.equals("YES") ? Optional.of(true)
                : (input.equals("NO") ? Optional.of(false)
                : Optional.empty());
        }
        
        return answer.get();
    }
    
    /**
     * Run the subsystem command through a given console.
     * @param console
     */
    void run (ConsoleManager console, CLAWSubsystem subsystem) throws TerminalKilledException {
        
        // Display description and an important safety warning
        console.println("Double-tap enter to disable the robot and stop the test command at any time.");
        
        console.println("Usage and description:");
        console.println(ConsoleManager.formatMessage(description, 2));
        
        // Wait until input after the driverstation is enabled
        boolean runCommand = getYesNo(console, "Run the command? ");
        if (!runCommand) return;
        
        // Retrieve the test command
        Command testCommand = testCommandSupplier.getCommand(subsystem, console);
        
        // Wait until the robot enables before running the test command, if required
        if (!testCommand.runsWhenDisabled()) {
            while (DriverStation.isDisabled()) {
                console.printlnErr("Enable the robot and try again.");
                runCommand = getYesNo(console, "Run the command? ");
                if (!runCommand) return;
            }
        }
        
        // Run the test command, one by one
        console.printlnSys("\nRunning test command.");
        console.flush();
        
        // Run the test command
        Action.fromCommand(testCommand).run();
        
        // TODO: Implement code to disable all the test sections on robot disable
        
        // Indicate that the test is finished
        console.printlnSys("\nTest concluded.");
        
    }
    
    /**
     * An interface supplying a {@link Command} for the {@link SubsystemTest}, and other basic information
     * on the subsystem test.
     */
    public static interface TestCommandSupplier {
        
        /**
         * Get a {@link TestCommandSupplier} from a given {@link SubsystemTestCompositionContext} consumer, which
         * uses the context to perform the subsystem test.
         * @param composition   The {@code Consumer<SubsystemTestCompositionContext>} which runs the test command.
         * @return              A {@code TestCommandSupplier} for the test command.
         */
        public static TestCommandSupplier fromComposition (Operation<SubsystemTestCompositionContext<?>> composition) {
            return (subsystem, console) -> SubsystemTestComposer.compose(console, subsystem, composition);
        }
        
        /**
         * This should return a {@link Command} which can run on the subsystem for the test.
         * @param subsystem The {@link CLAWSubsystem} this test section is linked to.
         * @param console   The {@link ConsoleManager} this test section will be linked to.
         */
        public Command getCommand (CLAWSubsystem subsystem, ConsoleManager console);
        
    }
    
}
