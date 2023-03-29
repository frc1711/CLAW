package claw.hardware.swerve.tests;

import claw.subsystems.SubsystemTest;

public class TurnSpeedTest extends SubsystemTest {
    
    public TurnSpeedTest () {
        super(
            "rotationTuner",
            "Tunes the rotation speeds mechanism (estimates kS and kV for robot rotation) allowing you " +
            "to create an accurate feedforward which estimates the speeds at which the robot can turn.",
            TestCommandSupplier.fromComposition(ctx -> {
                System.out.println("\n\nPRINTING TO CONSOLE\n\n");
                ctx.console.println("This is a test...");
            })
        );
    }
    
}
