// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import rct.low.DataMessage;
import rct.low.TerminalConnector;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    
    private Command m_autonomousCommand;
    private TerminalConnector terminalConnector;
    private RobotContainer m_robotContainer;

    /**
     * This function is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
        // autonomous chooser on the dashboard.
        m_robotContainer = new RobotContainer();
        terminalConnector = new TerminalConnector((DataMessage message) -> {
            System.out.println(message.dataString);
        }, false);
        
        while (true) {
            int millis = (int)Math.round(Math.random() * 18000);
            String msgString =
                "To: Driverstation; From: roboRIO.\n" +
                "  This message will be followed by " + millis/1000 + " seconds of silence.";
            terminalConnector.put(new DataMessage(DataMessage.MessageType.RESPONSE, 1, msgString));
            sleepAndUpdate(millis, terminalConnector);
        }
        
    }
    
    private void sleepAndUpdate (int millis, TerminalConnector c) {
        while (millis > 0) {
            try {
                Thread.sleep(20);
                millis -= 20;
                c.updateOutputBuffer();
            } catch (Exception e) {}
        }
    }
    
    @Override
    public void robotPeriodic() {
        terminalConnector.updateOutputBuffer();
        CommandScheduler.getInstance().run();
    }

    /** This function is called once each time the robot enters Disabled mode. */
    @Override
    public void disabledInit() {}

    @Override
    public void disabledPeriodic() {}

    /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
    @Override
    public void autonomousInit() {
        m_autonomousCommand = m_robotContainer.getAutonomousCommand();

        // schedule the autonomous command (example)
        if (m_autonomousCommand != null) {
            m_autonomousCommand.schedule();
        }
    }

    /** This function is called periodically during autonomous. */
    @Override
    public void autonomousPeriodic() {}

    @Override
    public void teleopInit() {
        if (m_autonomousCommand != null) {
            m_autonomousCommand.cancel();
        }
    }

    /** This function is called periodically during operator control. */
    @Override
    public void teleopPeriodic() {}

    @Override
    public void testInit() {
        // Cancels all running commands at the start of test mode.
        CommandScheduler.getInstance().cancelAll();
    }

    /** This function is called periodically during test mode. */
    @Override
    public void testPeriodic() {}

    /** This function is called once when the robot is first started up. */
    @Override
    public void simulationInit() {}

    /** This function is called periodically whilst in simulation. */
    @Override
    public void simulationPeriodic() {}
}
