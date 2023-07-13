package frc.robot.subsystems;

import claw.math.Vector;
import claw.math.VectorVelocityLimiter;
import claw.math.input.RaptorsXboxController;
import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import claw.subsystems.SubsystemTest.TestCommandSupplier;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.wpilibj2.command.PrintCommand;

public class TestSubsystem extends CLAWSubsystem {
    
    private final RaptorsXboxController controller = new RaptorsXboxController(0);
    private final VectorVelocityLimiter<N2> filter = new VectorVelocityLimiter<>(Vector.from(0, 0), 1);
    
    public TestSubsystem () {
        addTests(new SubsystemTest(
            "exampleTest",
            "Example description.",
            TestCommandSupplier.fromComposition(ctx -> {
                for (int i = 0; i < 5; i ++) {
                    ctx.run(new PrintCommand("This is a test print"));
                    ctx.delay(i * 0.2);
                }
            })
        ));
    }
    
    @Override
    public void stop () {
        
    }
    
    int i = 0;
    
    @Override
    public void periodic () {
        Vector<N2> filteredVector = filter.calculate(controller.getLeftStickAsVector());
        if (i % 5 == 0) {
            // System.out.println(filteredVector);
        }
        
        i ++;
    }
    
}
