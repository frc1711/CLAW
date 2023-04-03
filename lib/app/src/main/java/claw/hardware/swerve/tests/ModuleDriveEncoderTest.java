package claw.hardware.swerve.tests;

import claw.hardware.swerve.SwerveDriveHandler;
import claw.hardware.swerve.SwerveModuleBase;
import claw.subsystems.SubsystemTest;

public class ModuleDriveEncoderTest extends SubsystemTest {
    
    public ModuleDriveEncoderTest (SwerveDriveHandler swerveDrive) {
        super(
            "moduleDriveEncoder",
            "A test which can estimate drive encoder factors to get better distance measurements for swerve drive. This " +
            "is important for swerve drive odometry and, depending on the implementation details of the swerve modules " +
            "(whether they use a drive motor feedforward and PID loop) basic module functionality.",
            TestCommandSupplier.fromComposition(ctx -> {
                
                for (SwerveModuleBase module : swerveDrive.getModules()) {
                    ctx.console.printlnSys("Module: " + module.getIdentifier());
                    ctx.console.println("Prepare to count rotations. Press any key when you're ready.");
                }
                
            })
        );
    }
    
}
