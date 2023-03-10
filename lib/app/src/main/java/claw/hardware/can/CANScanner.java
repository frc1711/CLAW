package claw.hardware.can;

import claw.LiveValues;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.hal.CANStreamMessage;
import edu.wpi.first.hal.can.CANJNI;
import edu.wpi.first.hal.can.CANStatus;

/**
 * A utility class which can read messages from the CAN bus. Messages are read without distinction based on arbitration ID,
 * so the {@code CANScanner} should read messages from any active device on the CAN bus.
 */
public class CANScanner implements AutoCloseable {
    
    public static final CommandProcessor CAN_SCAN_COMMAND_PROCESSOR = new CommandProcessor(
        "canscan",
        "canscan",
        "Use 'canscan' to get the status of the CAN bus and to enumerate all active devices.",
        CANScanner::canScanCommand
    );
    
    private static void canScanCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        LiveValues values = new LiveValues();
        
        while (!console.hasInputReady()) {
            CANStatus status = getCANStatus();
            
            values.setField("busOffCount",              status.busOffCount);
            values.setField("percentBusUtilization",    status.percentBusUtilization);
            values.setField("receiveErrorCount",        status.receiveErrorCount);
            values.setField("transmitErrorCount",       status.transmitErrorCount);
            values.setField("txFullCount",              status.txFullCount);
        }
        
        console.printlnSys("\nReading 10 messages:");
        CANScanner scanner = new CANScanner();
        for (int i = 0; i < 10; i ++) {
            CANStreamMessage message = scanner.readMessage();
            printMessage(console, message);
            console.flush();
        }
        
        scanner.close();
    }
    
    private static void printMessage (ConsoleManager console, CANStreamMessage message) {
        console.printlnSys("Message ID: " + Integer.toBinaryString(message.messageID));
        CANMessageID messageID = CANMessageID.fromMessageId(message.messageID);
        console.printlnSys("  API Class: " + messageID.apiClass());
        console.printlnSys("  API Index: " + messageID.apiIndex());
        console.printlnSys("  Device Number: " + messageID.deviceNum());
        console.printlnSys("  Device Type: " + messageID.deviceType().name());
        console.printlnSys("  Manufacturer: " + messageID.manufacturer().friendlyName);
        
        console.printlnSys("Timestamp:  " + message.timestamp);
        console.printlnSys("Length:     " + message.length);
        console.printlnSys("-- Message Content --");
        for (byte b : message.data) {
            console.print(Byte.toString(b) + " ");
        }
    }
    
    /**
     * Get the status {@link CANStatus} of the CAN bus.
     * @return  The {@code CANStatus} describing the state of the CAN bus.
     */
    public static CANStatus getCANStatus () {
        CANStatus status = new CANStatus();
        CANJNI.getCANStatus(status);
        return status;
    }
    
    private final int sessionHandle;
    
    /**
     * Create a new {@link CANScanner} which can read any available messages on the CAN bus.
     */
    public CANScanner () {
        // messageID and messageIDMask are both 0 so all message IDs seen by the roboRIO
        // will match the messageID once the messageIDMask is applied
        sessionHandle = CANJNI.openCANStreamSession(0, 0, Integer.MAX_VALUE);
    }
    
    /**
     * Read a single message from the CAN bus. There is no guarantee that
     * the message will not be {@code null}.
     * @return  The latest message received from the CAN bus.
     */
    public CANStreamMessage readMessage () {
        return readMessages(1)[0];
    }
    
    /**
     * Reads {@code numMessages} messages from the CAN bus into an array.
     * There is no guarantee that message elements of the returned array
     * will not be {@code null}, but the length of the array is guaranteed to be equal to
     * {@code numMessages}.
     * @param numMessages   The number of messages read from the CAN bus.
     * @return              An array of messages read from the CAN bus.
     */
    public CANStreamMessage[] readMessages (int numMessages) {
        // Create a buffer of messages which will be populated by CANJNI
        CANStreamMessage[] messageBuffer = new CANStreamMessage[numMessages];
        
        // Read messages to fill the length of the messageBuffer
        CANJNI.readCANStreamSession(sessionHandle, messageBuffer, numMessages);
        
        // Return the messageBuffer which contains all the messages read from CAN
        return messageBuffer;
    }
    
    @Override
    public void close () {
        // Close the stream session handle
        CANJNI.closeCANStreamSession(sessionHandle);
    }
    
}
