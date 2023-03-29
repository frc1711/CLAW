package frc.robot.subsystems;

import claw.subsystems.CLAWSubsystem;
import claw.subsystems.SubsystemTest;
import claw.subsystems.SubsystemTest.TestCommandSupplier;
import edu.wpi.first.wpilibj2.command.PrintCommand;

public class TestSubsystem extends CLAWSubsystem {
    
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
    
}
