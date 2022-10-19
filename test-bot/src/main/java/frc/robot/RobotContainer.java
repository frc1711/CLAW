// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj2.command.Command;

import rct.ControlTerminal;

public class RobotContainer {
    public RobotContainer() {
        rct.ControlTerminal.testthing();
        
        System.out.println("I got this working");
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        NetworkTable table = inst.getTable("datatable");
        NetworkTableEntry xEntry = table.getEntry("x");
        NetworkTableEntry yEntry = table.getEntry("y");
        
        xEntry.setString("This string is being sent to entry 'x' from the robot!");
        yEntry.setString("yEntry (this string) is being sent over NetworkTables from the robot");
    }
    
    public Command getAutonomousCommand () {
        return null;
    }
}