package claw.hardware.can;

import java.nio.ByteBuffer;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import claw.LiveValues;
import claw.hardware.can.CANMessageID.DeviceType;
import claw.hardware.can.CANMessageID.ManufacturerCode;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.hal.can.CANJNI;
import edu.wpi.first.hal.can.CANMessageNotFoundException;
import edu.wpi.first.hal.can.CANStatus;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.math.filter.Debouncer.DebounceType;

/**
 * A utility class which can read messages from the CAN bus. Messages are read without distinction based on arbitration ID,
 * so the {@code CANScanner} should read messages from any active device on the CAN bus.
 */
public class CANScanner {
    
    public static record CANMessage (CANMessageID messageID, byte[] messageData, long timestamp, int intID) { }
    public static record CANDeviceTrace (DeviceType deviceType, ManufacturerCode manufacturer, int deviceNum) { }
    
    public static final CommandProcessor CAN_SCAN_COMMAND_PROCESSOR = new CommandProcessor(
        "canscan",
        "canscan [status | devices]",
        "Use 'canscan status' to get the status of the CAN bus (bus utilization and presence of errors). " +
        "'canscan devices' will scan to detect devices on the CAN bus. It reads manufacturers, device types, " +
        "and device numbers (IDs).",
        CANScanner::canScanCommand
    );
    
    private static double roundTo (double value, int precision) {
        return Math.round(value * precision) / precision;
    }
    
    private static String fillToSize (String str, int size) {
        int totalPadding = Math.max(size - str.length(), 0);
        
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        
        return " ".repeat(leftPadding) + str + " ".repeat(rightPadding);
    }
    
    private static void canScanCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        String scanType = reader.readArgOneOf("scan type", "Expected a scan type of 'status' or 'devices'.", "status", "devices");
        reader.noMoreArgs();
        reader.allowNoOptions();
        reader.allowNoFlags();
        
        LiveValues values = new LiveValues();
        
        if (scanType.equals("status")) {
            
            LinearFilter canUtilizationFilter = LinearFilter.movingAverage(35);
            Debouncer receiveErrorDebouncer = new Debouncer(0.1, DebounceType.kFalling);
            Debouncer transmitErrorDebouncer = new Debouncer(0.1, DebounceType.kFalling);
            
            while (!console.hasInputReady()) {
                
                CANStatus canStatusReading = getCANStatus();
                double percentBusUtilization = canUtilizationFilter.calculate(canStatusReading.percentBusUtilization) * 100;
                
                boolean hasReceiveError = receiveErrorDebouncer.calculate(canStatusReading.receiveErrorCount > 0);
                boolean hasTransmitError = transmitErrorDebouncer.calculate(canStatusReading.transmitErrorCount > 0);
                
                values.setField("Bus Utilization", roundTo(percentBusUtilization, 1000) + "%");
                values.setField("Receive Error", hasReceiveError ? "Present" : "None");
                values.setField("Transmit Error", hasTransmitError ? "Present" : "None");
                
                values.update(console);
            }
            
        } else if (scanType.equals("devices")) {
            
            console.printlnSys("Scanning...");
            console.flush();
            
            console.printlnSys(
                fillToSize("Manufacturer", 35) +
                fillToSize("Device Type", 35) +
                fillToSize("Device Number", 20)
            );
            
            Set<CANDeviceTrace> devices = scanCANDevices(250);
            for (CANDeviceTrace device : devices) {
                console.println(
                    fillToSize(device.manufacturer.friendlyName, 35) +
                    fillToSize(device.deviceType+"", 35) +
                    fillToSize(device.deviceNum+"", 20)
                );
            }
            
        }
        
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
    
    /**
     * Read a single message from the CAN bus. This can be {@code null}.
     * @return  A new message received from the CAN bus.
     */
    public static CANMessage readMessage () {
        
        ByteBuffer messageId = ByteBuffer.allocateDirect(4);
        messageId.order(ByteOrder.LITTLE_ENDIAN);
        
        messageId.clear();
        messageId.asIntBuffer().put(0, 0);
        
        ByteBuffer timestamp = ByteBuffer.allocate(4);
        
        byte[] message;
        try {
            message = CANJNI.FRCNetCommCANSessionMuxReceiveMessage(
                messageId.asIntBuffer(),
                0,
                timestamp
            );
        } catch (CANMessageNotFoundException e) {
            return null;
        }
        
        System.out.println("Next ID Position: " + messageId.asIntBuffer().position());
        
        int messageIdInt = messageId.asIntBuffer().get();
        
        // TODO: Use timestamp ByteBuffer to get message timestamp
        return new CANMessage(CANMessageID.fromMessageId(messageIdInt), message, 0, messageIdInt);
        
    }
    
    public static Set<CANDeviceTrace> scanCANDevices (int messagesToScan) {
        
        HashSet<CANDeviceTrace> devices = new HashSet<>();
        
        for (int i = 0; i < messagesToScan; i ++) {
            CANMessage msg = readMessage();
            
            if (msg != null) {
                CANDeviceTrace deviceTrace = new CANDeviceTrace(
                    msg.messageID().deviceType(),
                    msg.messageID().manufacturer(),
                    msg.messageID().deviceNum()
                );
                
                devices.add(deviceTrace);
            }
        }
        
        return devices;
        
    }
    
    private CANScanner () { }
    
}
