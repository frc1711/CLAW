package claw.hardware.swerve.tests;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.subsystems.SubsystemTest;

public class ModuleDriveTest extends SubsystemTest {
    
    public ModuleDriveTest (SwerveDriveHandler swerveDrive) {
        super(
            "moduleDriveTest",
            "A simple module test which drives the modules but does not turn them. The robot should be on its " +
            "side for the duration of this test.",
            TestCommandSupplier.fromComposition(ctx -> {
                ctx.console.println("Driving all modules up to speed...");
                
            })
        );
        
        
        
    }
    
}
