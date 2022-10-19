package frc.team1711.rct;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class Main {
    
    public static void main (String[] args) {
        new Main().run();
    }
    
    public void run () {
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        NetworkTable table = inst.getTable("datatable");
        NetworkTableEntry xEntry = table.getEntry("x");
        NetworkTableEntry yEntry = table.getEntry("y");
        inst.startClientTeam(1711);
        inst.startDSClient();
        
        int i = 1;
        
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("interrupted");
                return;
            }
            
            String noString = "[No String received over networktables]";
            String x = xEntry.getString(noString);
            String y = yEntry.getString(noString);
            System.out.println("\n\nIteration #"+i+"\n  X: " + x + "\n  Y: " + y);
            
            i ++;
        }
    }
    
}