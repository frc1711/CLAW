package claw;

import java.util.function.Supplier;

import claw.internal.CLAWRuntime;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;

public class CLAWRobot {
    
    /**
     * Get a {@code Supplier<RobotBase>} that provides a {@link RobotBase} proxy which CLAW can use. This robot proxy
     * will also start all necessary CLAW processes. This method should only be used in {@code Main.java} as a wrapper
     * around {@code Robot::new}.
     * @param robotSupplier A {@code Supplier<TimedRobot>} which can be used to get a new robot object.
     * @return              The {@code Supplier<RobotBase>} containing the CLAW robot proxy.
     */
    public static Supplier<RobotBase> fromRobot (Supplier<TimedRobot> robotSupplier) {
        return new Supplier<RobotBase>(){
            @Override
            public RobotBase get () {
                TimedRobot robot = robotSupplier.get();
                CLAWRuntime.initialize();
                robot.addPeriodic((CLAWRuntime.getInstance())::robotPeriodic, TimedRobot.kDefaultPeriod);
                return robot;
            }
        };
    }
    
}
