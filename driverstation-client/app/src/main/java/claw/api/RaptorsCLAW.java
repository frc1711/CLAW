package claw.api;

import java.util.function.Supplier;

import edu.wpi.first.wpilibj.RobotBase;

public class RaptorsCLAW {
    
    
    
    // RaptorsCLAW singleton initialization with fromRobot (called in Main.java) and retrieval with getInstance
    
    private static RaptorsCLAW instance = null;
    
    public static RaptorsCLAW getInstance () {
        if (instance == null)
            throw new RuntimeException("RaptorsCLAW has not been initialized in Main.java");
        return instance;
    }
    
    /**
     * Get a {@code Supplier<RobotBase>} that provides a {@link RobotBase} proxy which CLAW can use. This robot proxy
     * will also start all necessary CLAW processes. This method should only be used in {@code Main.java} as a wrapper
     * around {@code Robot::new}.
     * @param robotSupplier A {@code Supplier<RobotBase>} which can be used to get a new robot object.
     * @return              The {@code Supplier<RobotBase>} containing the CLAW robot proxy.
     */
    public static Supplier<RobotBase> fromRobot (Supplier<RobotBase> robotSupplier) {
        return new Supplier<RobotBase>(){
            @Override
            public RobotBase get () {
                if (instance == null)
                    instance = new RaptorsCLAW(robotSupplier);
                return instance.robotProxy;
            }
        };
    }
    
    
    
    // RaptorsCLAW instance methods
    
    private final RobotProxy robotProxy;
    
    private RaptorsCLAW (Supplier<RobotBase> robotSupplier) {
        this.robotProxy = new RobotProxy(robotSupplier);
    }
    
    private void handleUncaughtThrowable (Throwable throwable) {
        System.out.println("\n\n\nCaught an uncaught throwable: " + throwable.getMessage());
        System.exit(0);
    }
    
    
    
    // RobotProxy instance methods
    
    private class RobotProxy extends RobotBase {
        
        private final RobotBase robot;
        
        public RobotProxy (Supplier<RobotBase> robotSupplier) {
            robot = robotSupplier.get();
        }
        
        @Override
        public void startCompetition () {
            try {
                robot.startCompetition();
            } catch (Throwable throwable) {
                handleUncaughtThrowable(throwable);
            }
        }
        
        @Override
        public void endCompetition () {
            robot.endCompetition();
        }
        
    }
    
}
