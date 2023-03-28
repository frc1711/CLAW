// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import claw.actions.CommandComposer;
import claw.logs.CLAWLogger;
import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.commands.TestCommand;
import frc.robot.subsystems.TestSubsystem;

public class RobotContainer {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("robotcontainer");
    private final TestSubsystem testSubsystem = new TestSubsystem();
    private final TestCommand testCommand = new TestCommand(testSubsystem);
    
    public RobotContainer () {
        LOG.out("Starting up RobotContainer");
        testSubsystem.setDefaultCommand(testCommand);
    }
    
    public Command getAutonomousCommand () {
        return CommandComposer.getComposition(ctx -> {
            ctx.run(new TestCommand(testSubsystem).withTimeout(5));
            
            for (int i = 0; i < 3; i ++) {
                ctx.run(new PrintCommand("Random: " + Math.random()));
                ctx.delay(0.5);
            }
        });
    }
}