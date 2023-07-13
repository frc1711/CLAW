// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import claw.logs.CLAWLogger;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.TestCommand;
import frc.robot.subsystems.TestSubsystem;
import frc.robot.subsystems.swerve.SwerveSubsystem;

public class RobotContainer {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("robotcontainer");
    
    private final TestSubsystem testSubsystem = new TestSubsystem();
    private final SwerveSubsystem swerveSubsystem = new SwerveSubsystem();
    
    private final TestCommand testCommand = new TestCommand(testSubsystem);
    
    public RobotContainer () {
        LOG.out("Starting up RobotContainer");
        testSubsystem.setDefaultCommand(testCommand);
    }
    
    public Command getAutonomousCommand () {
        return null;
    }
}