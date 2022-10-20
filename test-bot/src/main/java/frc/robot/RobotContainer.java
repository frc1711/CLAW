// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj2.command.Command;

import rct.low.TerminalConnector;

public class RobotContainer {
    public RobotContainer() {
        TerminalConnector c = new TerminalConnector(false);
        c.makeCall("test-call".getBytes());
    }
    
    public Command getAutonomousCommand () {
        return null;
    }
}