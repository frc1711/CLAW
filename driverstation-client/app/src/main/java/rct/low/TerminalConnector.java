/**
 * Created and maintained by FRC Team 1711
 */

package rct.low;

import java.util.function.Consumer;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class TerminalConnector {
    
    private static final String
        NETWORK_TABLE_NAME = "ROBOT_CONTROL_TERMINAL_NETWORKTABLE_HANDLE",
        DRIVERSTATION_OUT_ENTRY_NAME = "DRIVERSTATION_OUTPUT",
        ROBOT_OUT_ENTRY_NAME = "ROBOT_OUTPUT";
    
    private static boolean connectorExists = false;
    
    private final NetworkTableInstance networkTableInstance;
    private final NetworkTable networkTable;
    private final boolean isDriverStation;
    
    private final NetworkTableEntry tableInputEntry, tableOutputEntry;
    
    public TerminalConnector (boolean isDriverStation) {
        // Do not create a second TerminalConnector on the same client script
        if (connectorExists) throw new RuntimeException("Cannot create a second TerminalConnector on the same client");
        connectorExists = true;
        
        // Start the networktable
        this.isDriverStation = isDriverStation;
        networkTableInstance = NetworkTableInstance.getDefault();
        if (isDriverStation) {
            networkTableInstance.startClientTeam(1711);
            networkTableInstance.startDSClient();
        }
        
        networkTable = networkTableInstance.getTable(NETWORK_TABLE_NAME);
        
        // Get the two networktable entries
        final NetworkTableEntry driverstationOut = networkTable.getEntry(DRIVERSTATION_OUT_ENTRY_NAME);
        final NetworkTableEntry robotOut = networkTable.getEntry(ROBOT_OUT_ENTRY_NAME);
        tableInputEntry = isDriverStation ? robotOut : driverstationOut;
        tableOutputEntry = isDriverStation ? driverstationOut : robotOut;
        
        // Prepare the two networktable entries
        tableOutputEntry.forceSetRaw(new byte[0]);
        tableInputEntry.addListener(new Consumer<EntryNotification>(){
            @Override
            public void accept(EntryNotification t) {
                byte[] val = t.value.getRaw();
                String valStr = "[";
                for (byte b : val) {
                    valStr += b + ", ";
                } valStr += "]";
                
                System.out.println("Received value:\n"+valStr);
            }
        }, EntryListenerFlags.kUpdate);
    }
    
    public void makeCall (byte[] value) {
        if (tableOutputEntry.exists()) {
            tableOutputEntry.forceSetRaw(value);
        }
    }
    
}