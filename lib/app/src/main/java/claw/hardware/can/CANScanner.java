package claw.hardware.can;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import java.util.Set;
import java.util.HashSet;

import java.nio.ByteOrder;
import claw.LiveValues;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.hal.CANAPIJNI;
import edu.wpi.first.hal.CANStreamMessage;
import edu.wpi.first.hal.can.CANJNI;
import edu.wpi.first.hal.can.CANStatus;

/**
 * A utility class which can read messages from the CAN bus. Messages are read without distinction based on arbitration ID,
 * so the {@code CANScanner} should read messages from any active device on the CAN bus.
 */
public class CANScanner implements AutoCloseable {
    
    public static record CANMessage (CANMessageID messageID, byte[] messageData, long timestamp, int intID) { }
    
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
            
            values.update(console);
        }
        
        CANScanner scanner = new CANScanner();
        Set<Integer> messageIds = new HashSet<Integer>();
        
        console.readInputLine();
        
        while (!console.hasInputReady()) {
            for (int i = 0; i < 100; i ++) {
                try {
                    CANMessage message = scanner.readMessage();
                    messageIds.add(message.intID());
                } catch (Exception e) { }
            }
            console.printlnSys("what");
        }
        
        for (int id : messageIds) {
            printMessageId(console, id, CANMessageID.fromMessageId(id));
        }
        
        // for (int i = 0; i < 100; i ++) {
        //     CANMessage message = scanner.readMessage();
        //     printMessage(console, message);
        //     console.flush();
        // }
        
        scanner.close();
    }
    
    private static void printMessageId (ConsoleManager console, int idInt, CANMessageID id) {
        String byteRepr = Integer.toBinaryString(idInt);
        byteRepr = "0".repeat(Math.min(32, 32 - byteRepr.length())) + byteRepr;
        
        console.printlnSys("Message ID: " + byteRepr);
        console.printlnSys("  API Class: " + id.apiClass());
        console.printlnSys("  API Index: " + id.apiIndex());
        console.printlnSys("  Device Number: " + id.deviceNum());
        console.printlnSys("  Device Type: " + id.deviceType().name());
        console.printlnSys("  Manufacturer: " + id.manufacturer().friendlyName);
    }
    
    private static void printMessage (ConsoleManager console, CANMessage message) {
        if (message != null) {
            printMessageId(console, message.intID(), message.messageID());
            // console.printlnSys("Timestamp:  " + message.timestamp);
            // console.printlnSys("Length:     " + message.length);
            console.printlnSys("-- Message Content --");
            for (byte b : message.messageData()) {
                console.print(Byte.toString(b) + " ");
            }
            console.println("");
            
        } else {
            console.printlnSys("Null message received");
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
    public CANMessage readMessage () {
        
        ByteBuffer messageId = ByteBuffer.allocateDirect(4);
        messageId.order(ByteOrder.LITTLE_ENDIAN);
        
        messageId.clear();
        messageId.asIntBuffer().put(0, 0);
        
        ByteBuffer timestamp = ByteBuffer.allocate(4);
        
        byte[] message = CANJNI.FRCNetCommCANSessionMuxReceiveMessage(
            messageId.asIntBuffer(),
            0,
            timestamp
        );
        
        int messageIdInt = messageId.get();
        System.out.println("Message ID: " + Integer.toBinaryString(messageIdInt));
        System.out.println("Device Type: " + CANMessageID.fromMessageId(messageIdInt).deviceType());
        
        // TODO: Use timestamp ByteBuffer to get message timestamp
        return new CANMessage(CANMessageID.fromMessageId(messageIdInt), message, 0, messageIdInt);
        
    }
    
    /**
     * Reads {@code numMessages} messages from the CAN bus into an array.
     * There is no guarantee that message elements of the returned array
     * will not be {@code null}, but the length of the array is guaranteed to be equal to
     * {@code numMessages}.
     * @param numMessages   The number of messages read from the CAN bus.
     * @return              An array of messages read from the CAN bus.
     */
    public CANMessage[] readMessages (int numMessages) {
        
        return new CANMessage[0];
        
        // System.out.println("Message ID: " + messageId.get());
        // for (byte b : message) {
        //     System.out.print(b + " ");
        // }
        
        // System.out.println("\n");
        
        // CANStreamMessage msg = new CANStreamMessage();
        // msg.setStreamData(1, 0, 0);
        
        // return new CANMessage();
        
        // // Create a buffer of messages which will be populated by CANJNI
        // CANStreamMessage[] messageBuffer = new CANStreamMessage[numMessages];
        
        // // Read messages to fill the length of the messageBuffer
        // new Thread(() -> CANJNI.readCANStreamSession(sessionHandle, messageBuffer, numMessages)).start();
        
        // try {
        //     Object obj = new Object();
        //     synchronized (obj) {
        //         obj.wait(500);
        //     }
        // } catch (InterruptedException e) {
            
        // }
        
        // // Return the messageBuffer which contains all the messages read from CAN
        // return messageBuffer;
    }
    
    @Override
    public void close () {
        // Close the stream session handle
        CANJNI.closeCANStreamSession(sessionHandle);
    }
    
}
