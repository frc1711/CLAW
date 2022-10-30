/**
 * Created and maintained by FRC Team 1711
 */

package rct.low;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    
    /**
     * Whether or not a TerminalConnector already exists in this JVM instance
     */
    private static boolean connectorExists = false;
    
    // NetworkTables fields
    private final NetworkTableInstance networkTableInstance;
    private final NetworkTable networkTable;
    private final NetworkTableEntry tableInputEntry, tableOutputEntry;
    
    // Sending messages
    private final List<Byte[]> outputBuffer = new ArrayList<Byte[]>();
    private long lastUpdateTime = 0;
    private int nextOutputBufferId = 1;
    
    // Receiving messages
    private final Consumer<DataMessage> messageReceiver;
    
    // Misc
    private final boolean isDriverStation;
    
    public TerminalConnector (Consumer<DataMessage> messageReceiver, boolean isDriverStation) {
        // Do not create a second TerminalConnector on the same client script
        if (connectorExists) throw new RuntimeException("Cannot create a second TerminalConnector on the same client");
        connectorExists = true;
        
        // Start the networktable
        networkTableInstance = NetworkTableInstance.getDefault();
        
        // Prepares networktables for driver station
        this.isDriverStation = isDriverStation;
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
                handleCriticalException(new Exception("Critical NetworkTables Error: " + message.message));
            }, LogMessage.kCritical, LogMessage.kCritical);
        }
        
        // Get the two networktable entries
        final NetworkTableEntry driverstationOut = networkTable.getEntry(DRIVERSTATION_OUT_ENTRY_NAME);
        final NetworkTableEntry robotOut = networkTable.getEntry(ROBOT_OUT_ENTRY_NAME);
        tableInputEntry = isDriverStation ? robotOut : driverstationOut;
        tableOutputEntry = isDriverStation ? driverstationOut : robotOut;
        
        // Prepare the two networktable entries for sending and receiving data
        tableOutputEntry.forceSetRaw(new byte[]{});
        tableInputEntry.addListener((EntryNotification n) -> {
            TerminalConnector.this.acceptInput(n.value.getRaw(), n.value.getTime());
        }, EntryListenerFlags.kUpdate);
        
        this.messageReceiver = messageReceiver;
    }
    
    /**
     * Handles the input of raw binary over NetworkTables which represents a buffer of several messages.
     * Once the buffer is read, each individual message is sent to {@link #acceptMessageInput(byte[])}
     */
    private void acceptInput (byte[] input, long time) {
        
        // Do nothing if no bytes are received
        if (input.length == 0) return;
        
        // Attempt to process the data input as a RawMessagesBuffer
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(input));
        try {
            RawMessagesBuffer buffer = RawMessagesBuffer.readFromStream(dataIn);
            
            // Process each message received in the buffer
            for (byte[] message : buffer.messagesData)
                acceptMessageInput(message);
            
        } catch (IOException e) {
            handleCriticalException(e);
        }
        
    }
    
    /**
     * Handles the input of a raw binary message by attempting to convert the binary to a {@link DataMessage}.
     */
    private void acceptMessageInput (byte[] rawData) {
        try {
            DataMessage message = DataMessage.readFrom(new ByteArrayInputStream(rawData));
            messageReceiver.accept(message);
        } catch (DataMessage.MalformedMessageException e) {
            handleCriticalException(e);
        }
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
        if (networkTableInstance.isConnected()) {
            // Get a serializable object representing the several messages stored
            // in the outputBuffer
            RawMessagesBuffer messages = new RawMessagesBuffer(nextOutputBufferId, outputBuffer);
            
            // Set the networktables data to match the serialized RawMessagesBuffer
            tableOutputEntry.forceSetRaw(messages.getSerializedForm());
            
            // Update the nextOutputBufferId so that the next message will follow
            nextOutputBufferId ++;
            
            // Set lastUpdateTime to the current time to prevent
            // updateOutputBuffer from working again too soon
            lastUpdateTime = System.currentTimeMillis();
            
            // Clears the output buffer
            outputBuffer.clear();
        }
    }
    
    /**
     * Puts a {@link DataMessage} to the output buffer to be sent during some updateOutputBuffer call
     */
    public void put (DataMessage message) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            message.putBytesTo(byteStream);
        } catch (DataMessage.MalformedMessageException e) {
            System.out.println(e);
        }
        
        putRawData(byteStream.toByteArray());
    }
    
    /**
     * Puts raw data to the output buffer and attempts to update the output buffer
     */
    private void putRawData (byte[] value) {
        // Converts byte[] value to Byte[] so it can be stored in the List<Byte[]> outputBuffer
        final Byte[] byteObjs = new Byte[value.length];
        for (int i = 0; i < value.length; i ++)
            byteObjs[i] = value[i];
        
        outputBuffer.add(byteObjs);
        updateOutputBuffer();
    }
    
    /**
     * Display the error and kill the program after a few seconds
     */
    private void handleCriticalException (Exception e) {
        if (isDriverStation) {
            // Print the error
            System.err.println("\n\nFATAL ERROR:");
            System.err.println(e);
            
            // Attempt to wait for 5 seconds before killing the program
            try { Thread.sleep(5000);
            } catch (InterruptedException ex) { }
            
            // Kill the program
            System.exit(1);
        } else {
            System.err.println(e);
        }
    }
    
}