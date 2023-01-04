package claw;

import java.io.IOException;
import java.util.function.Supplier;

import claw.devices.Config;
import claw.logs.LogHandler;
import claw.logs.RCTLog;
import claw.rct.remote.RCTServer;
import claw.subsystems.SubsystemCLAW;
import claw.subsystems.SubsystemRegistry;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class CLAWRuntime {
    
    
    // Stream names
    
    private static final RCTLog COMMANDS_LOG = LogHandler.getInstance().getSysLog("Commands");
    
    
    
    // CLAWRuntime singleton initialization with fromRobot (called in Main.java) and retrieval with getInstance
    
    private static CLAWRuntime instance = null;
    
    public static CLAWRuntime getInstance () {
        // This runtime exception should never ever happen, as there is practically zero possibility
        // of anyone trying to access a CLAWRuntime instance before it is initialized by the
        // fromRobot supplier sent to RobotBase.startRobot in Main.java.
        
        // The only case in which this can reasonably happen is if the CLAWRuntime entry point
        // has not yet been added to Main.java
        if (instance == null)
            throw new RuntimeException("CLAWRuntime has not been initialized in Main.java");
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
                
                // When this supplier is called in RobotBase.startRobot, it will initialize the CLAWRuntime instance
                if (instance == null)
                    instance = new CLAWRuntime(robotSupplier);
                return instance.robotProxy;
            }
        };
    }
    
    
    
    // CLAWRuntime private methods
    
    private final SubsystemRegistry subsystemRegistry = new SubsystemRegistry();
    private final RobotProxy robotProxy;
    private RCTServer server;
    
    private CLAWRuntime (Supplier<TimedRobot> robotSupplier) {
        // Put a message into the console indicating that the CLAWRuntime runtime has started
        System.out.println("\n -- CLAWRuntime is running -- \n");
        
        // Initialize the robot proxy
        robotProxy = new RobotProxy(robotSupplier);
        
        // Start the RCT server in another thread (so that the server startup is non-blocking)
        new Thread(() -> {
            try {
                server = new RCTServer(5800, subsystemRegistry);
                server.start();
            } catch (IOException e) {
                System.err.println("Failed to start RCT server.");
                e.printStackTrace();
            }
        }).start();
    }
    
    private void handleUncaughtThrowable (Throwable throwable) {
        System.err.println("\n\n\nCaught an uncaught throwable: " + throwable.getMessage());
    }
    
    /**
     * The robot proxy schedules this method to be called at the default TimedRobot period
     */
    private void robotPeriodic () {
        if (server != null)
            LogHandler.getInstance().sendData(server);
    }
    
    /**
     * The robot proxy calls this method before the startCompetition method exits (regardless
     * of whether any exceptions have been thrown) so that important operations can be finished.
     */
    private void onRobotProgramExit () {
        LogHandler.getInstance().saveUnsentData();
        
        try {
            Config.getInstance().save();
        } catch (IOException e) {
            System.err.println("Failed to save CLAW config data: " + e.getMessage());
        }
    }
    
    private void onCommandInitialize (Command command) {
        COMMANDS_LOG.out(command.getName() + " initialized");
    }
    
    private void onCommandExecute (Command command) {
        
    }
    
    private void onCommandFinish (Command command) {
        COMMANDS_LOG.out(command.getName() + " finished");
    }
    
    private void onCommandInterrupt (Command command) {
        COMMANDS_LOG.out(command.getName() + " was interrupted");
    }
    
    
    
    // Public API
    
    public void addSubsystem (SubsystemCLAW subsystem) {
        subsystemRegistry.addSubsystem(subsystem);
    }
    
    public void restartCode () {
        onRobotProgramExit();
        System.exit(0);
    }
    
    
    
    // RobotProxy instance methods
    
    private class RobotProxy extends RobotBase {
        
        private final TimedRobot robot;
        
        public RobotProxy (Supplier<TimedRobot> robotSupplier) {
            robot = robotSupplier.get();
            
            // Schedule the robotPeriodic method to be called at the default TimedRobot period
            robot.addPeriodic(CLAWRuntime.this::robotPeriodic, TimedRobot.kDefaultPeriod);
            
            CommandScheduler.getInstance().onCommandInitialize(CLAWRuntime.this::onCommandInitialize);
            CommandScheduler.getInstance().onCommandExecute(CLAWRuntime.this::onCommandExecute);
            CommandScheduler.getInstance().onCommandFinish(CLAWRuntime.this::onCommandFinish);
            CommandScheduler.getInstance().onCommandInterrupt(CLAWRuntime.this::onCommandInterrupt);
        }
        
        @Override
        public void startCompetition () {
            try {
                robot.startCompetition();
                onRobotProgramExit();
            } catch (Throwable throwable) {
                handleUncaughtThrowable(throwable);
                onRobotProgramExit();
                throw throwable;
            }
        }
        
        @Override
        public void endCompetition () {
            robot.endCompetition();
        }
        
    }
    
}
