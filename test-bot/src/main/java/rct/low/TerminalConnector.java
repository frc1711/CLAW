/**
 * Created and maintained by FRC Team 1711
 */

package rct.low;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.LogMessage;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class TerminalConnector {
    
    private static final String
        NETWORK_TABLE_NAME = "ROBOT_CONTROL_TERMINAL_NETWORK_TABLE",
        DRIVERSTATION_OUT_ENTRY_NAME = "DRIVERSTATION_OUTPUT",
        ROBOT_OUT_ENTRY_NAME = "ROBOT_OUTPUT";
    
    private static final long
        UPDATE_INTERVAL_MILLIS = 600;
    
    private static boolean connectorExists = false;
    
    private final NetworkTableInstance networkTableInstance;
    private final NetworkTable networkTable;
    private final NetworkTableEntry tableInputEntry, tableOutputEntry;
    
    private final List<Byte[]> outputBuffer = new ArrayList<Byte[]>();
    private long lastUpdateTime = 0;
    private int rawMessagesBufferId = 1;
    
    public TerminalConnector (boolean isDriverStation) {
        // Do not create a second TerminalConnector on the same client script
        if (connectorExists) throw new RuntimeException("Cannot create a second TerminalConnector on the same client");
        connectorExists = true;
        
        // Start the networktable
        networkTableInstance = NetworkTableInstance.getDefault();
        
        // Prepares networktables for driver station
        if (isDriverStation) {
            networkTableInstance.startClientTeam(1711);
            networkTableInstance.startDSClient();
        }
        
        networkTable = networkTableInstance.getTable(NETWORK_TABLE_NAME);
        
        // Normally, a lot of extra info about the connection is put to stderr
        // and this messes with the driverstation-side app. networkTableInstance.addLogger
        // prevents this and reroutes log messages
        if (isDriverStation) {
            networkTableInstance.addLogger((LogMessage message) -> {
                // Captures all LogMessages instead of sending to stderr
                System.err.println("Critical NetworkTables Error: " + message.message);
            }, LogMessage.kCritical, LogMessage.kCritical);
        }
        
        // Get the two networktable entries
        final NetworkTableEntry driverstationOut = networkTable.getEntry(DRIVERSTATION_OUT_ENTRY_NAME);
        final NetworkTableEntry robotOut = networkTable.getEntry(ROBOT_OUT_ENTRY_NAME);
        tableInputEntry = isDriverStation ? robotOut : driverstationOut;
        tableOutputEntry = isDriverStation ? driverstationOut : robotOut;
        
        // Prepare the two networktable entries
        tableOutputEntry.forceSetRaw(new byte[]{});
        tableInputEntry.addListener((EntryNotification n) -> {
            TerminalConnector.this.acceptInput(n.value.getRaw(), n.value.getTime());
        }, EntryListenerFlags.kUpdate);
    }
    
    private void acceptInput (byte[] input, long time) {
        System.out.println("This is serialized:");
        System.out.println(" - Received input bytes with length: " + input.length);
        System.out.println("       ct: "+time + ", pt: "+tableInputEntry.getLastChange());
    }

    /**
     * Takes all data from the outputBuffer, converts it to a RawMessagesBuffer (a
     * serializable object holding an array of raw byte[] messages), and sends the messages.
     */
    public void updateOutputBuffer () {
        // If there is nothing new to send, send nothing
        if (outputBuffer.size() == 0) return;
        
        // If the output buffer has been updated recently, return without doing anything
        // so that NetworkTables isn't updated more than once before being flushed
        if (System.currentTimeMillis() < lastUpdateTime + UPDATE_INTERVAL_MILLIS) return;
        
        // If unable to connect or set the data for whatever reason, simply do not
        // attempt to send it yet
        if (networkTableInstance.isConnected() && tableOutputEntry.exists()) {
            
            // Get a serializable object representing the several messages stored
            // in the outputBuffer
            RawMessagesBuffer messages = new RawMessagesBuffer(rawMessagesBufferId, outputBuffer);
            ByteArrayOutputStream bytesOutStream = new ByteArrayOutputStream();
            try {
                ObjectOutputStream stream = new ObjectOutputStream(bytesOutStream);
                stream.writeObject(messages);
            } catch (IOException e) {
                // This should never happen as it is simply writing to an ByteArrayOutputStream
                throw new RuntimeException("Serialization of RawMessagesBuffer failed: " + e.getMessage());
            }
            
            // Set the networktables data to match the serialized RawMessagesBuffer
            tableOutputEntry.forceSetRaw(bytesOutStream.toByteArray());
            
            // Update the rawMessagesBufferId so that the next message will follow
            rawMessagesBufferId ++;
            
            // Set lastUpdateTime to the current time to prevent
            // updateOutputBuffer from working again too soon
            lastUpdateTime = System.currentTimeMillis();
            
            // Clears the output buffer
            outputBuffer.clear();
        }
    }
    
    public void put (byte[] value) {
        // Converts byte[] value to Byte[] so it can be stored in the List<Byte[]> outputBuffer
        final Byte[] byteObjs = new Byte[value.length];
        for (int i = 0; i < value.length; i ++)
            byteObjs[i] = value[i];
        
        outputBuffer.add(byteObjs);
    }
    
}