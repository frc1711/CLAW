// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import claw.logs.LogHandler;
import claw.logs.RCTLog;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.TestCommand;
import frc.robot.subsystems.TestSubsystem;

public class RobotContainer {
    
    private final RCTLog LOG = LogHandler.getInstance().getLog("RobotContainer");
    private final TestSubsystem testSubsystem = new TestSubsystem();
    private final TestCommand testCommand = new TestCommand(testSubsystem);
    
    public RobotContainer () {
        LOG.out("Starting up RobotContainer");
        testSubsystem.setDefaultCommand(testCommand);
    }
    
    public Command getAutonomousCommand () {
        return null;
    }
}