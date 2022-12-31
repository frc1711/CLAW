package claw.api;

import java.io.IOException;
import java.util.function.Supplier;

import claw.rct.remote.RCTServer;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class RaptorsCLAW {
    
    
    
    // RaptorsCLAW singleton initialization with fromRobot (called in Main.java) and retrieval with getInstance
    
    private static RaptorsCLAW instance = null;
    
    public static RaptorsCLAW getInstance () {
        // This runtime exception should never ever happen, as there is practically zero possibility
        // of anyone trying to access a RaptorsCLAW instance before it is initialized by the
        // fromRobot supplier sent to RobotBase.startRobot in Main.java.
        
        // The only case in which this can reasonably happen is if the RaptorsCLAW entry point
        // has not yet been added to Main.java
        if (instance == null)
            throw new RuntimeException("RaptorsCLAW has not been initialized in Main.java");
        return instance;
    }
    
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
                
                // When this supplier is called in RobotBase.startRobot, it will initialize the RaptorsCLAW instance
                if (instance == null)
                    instance = new RaptorsCLAW(robotSupplier);
                return instance.robotProxy;
            }
        };
    }
    
    
    
    // RaptorsCLAW instance methods
    
    private final RobotProxy robotProxy;
    private RCTServer server;
    
    private RaptorsCLAW (Supplier<TimedRobot> robotSupplier) {
        System.out.println("\n -- RaptorsCLAW is running -- \n");
        
        robotProxy = new RobotProxy(robotSupplier);
        new Thread(() -> {
            try {
                server = new RCTServer(5800);
                server.start();
            } catch (IOException e) {
                System.out.println("Failed to start RCT server.");
                e.printStackTrace();
            }
        }).start();
    }
    
    private void handleUncaughtThrowable (Throwable throwable) {
        System.out.println("\n\n\nCaught an uncaught throwable: " + throwable.getMessage());
    }
    
    private void robotPeriodic () {
        if (robotProxy.isEnabled()) {
            System.out.println("Robot periodic");
        }
    }
    
    public void restartCode () {
        System.out.print("\n".repeat(5));
        System.exit(0);
    }
    
    
    
    // RobotProxy instance methods
    
    private class RobotProxy extends RobotBase {
        
        private final TimedRobot robot;
        
        public RobotProxy (Supplier<TimedRobot> robotSupplier) {
            robot = robotSupplier.get();
            robot.addPeriodic(RaptorsCLAW.this::robotPeriodic, TimedRobot.kDefaultPeriod);
        }
        
        @Override
        public void startCompetition () {
            try {
                robot.startCompetition();
            } catch (Throwable throwable) {
                handleUncaughtThrowable(throwable);
                throw throwable;
            }
        }
        
        @Override
        public void endCompetition () {
            robot.endCompetition();
        }
        
    }
    
}
