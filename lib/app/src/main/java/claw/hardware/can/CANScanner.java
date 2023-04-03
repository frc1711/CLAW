package claw.hardware.can;

import java.nio.ByteBuffer;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import claw.LiveValues;
import claw.actions.compositions.Context.TerminatedContextException;
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
 * A utility class which can detect devices on, send messages to, and read messages from the CAN bus.
 */
public class CANScanner {
    
    /**
     * A message read from the CAN bus.
     */
    public static record CANMessage (CANMessageID messageID, byte[] messageData, long timestamp) { }
    
    /**
     * A trace for a device connected to the roboRIO via CAN.
     */
    public static record CANDeviceTrace (DeviceType deviceType, ManufacturerCode manufacturer, int deviceNum) { }
    
    /**
     * A command processor for the {@code canscan} command.
     */
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
    
    private static String padToSize (String str, int size) {
        int totalPadding = Math.max(size - str.length(), 0);
        
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        
        return " ".repeat(leftPadding) + str + " ".repeat(rightPadding);
    }
    
    private static void canScanCommand (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException {
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
            
            // TODO: Send an "enumerate" CAN frame, write a wrapper around the FRC_Net_Comm_Mux functions,
            // filter out bad data, remove System.out printing, and clean up CANMessageIDs
            
            console.printlnSys("Scanning...");
            console.flush();
            
            console.printlnSys(
                padToSize("Manufacturer", 35) +
                padToSize("Device Type", 35) +
                padToSize("Device Number", 20)
            );
            
            Set<CANDeviceTrace> devices = scanCANDevices(600);
            for (CANDeviceTrace device : devices) {
                console.println(
                    padToSize(device.manufacturer.friendlyName, 35) +
                    padToSize(device.deviceType+"", 35) +
                    padToSize(device.deviceNum+"", 20)
                );
            }
            
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
     * Read a single message from the CAN bus (from any device).
     * @return  A new message received from the CAN bus.
     */
    public static Optional<CANMessage> readMessage () {
        return readMessage(0, 0);
    }
    
    /**
     * Read a single message from the CAN bus. See the FRC CAN Device Specifications for
     * the specifications on the message ID.
     * https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html
     * <br></br>
     * Note that there are generally better options through WPILib for reading messages
     * from the CAN bus. This would only be useful if you need to read a broad range of
     * messages from varying IDs for some reason.
     * @param messageID         The arbitration ID of the messages to read. See the specifications.
     * @param messageIDMask     A bit mask to apply to messages IDs received from the CAN bus.
     * The received message ID, after applying a bitwise {@code &} with the given mask, will be checked
     * against the {@code messageID} argument to see if it should be intercepted.
     * @return                  The CAN message intercepted from the bus, if one could be read.
     */
    public static Optional<CANMessage> readMessage (int messageID, int messageIDMask) {
        
        // Create a message ID buffer for distinguishing which messages to intercept
        // This buffer will also be filled with the ID of the retrieved message once done
        ByteBuffer messageIDBuffer = ByteBuffer.allocateDirect(4);
        messageIDBuffer.order(ByteOrder.LITTLE_ENDIAN);
        messageIDBuffer.clear();
        messageIDBuffer.asIntBuffer().put(messageID, 0);
        
        // Create a buffer to store the timestamp of the received message. This will be filled
        // after receiving the message
        ByteBuffer timestampBuffer = ByteBuffer.allocate(4);
        
        byte[] messageContent;
        
        try {
            // Receive a message from the CAN bus and fill the messageIDBuffer and timestampBuffer
            messageContent = CANJNI.FRCNetCommCANSessionMuxReceiveMessage(
                messageIDBuffer.asIntBuffer(),
                messageIDMask,
                timestampBuffer
            );
        } catch (CANMessageNotFoundException e) {
            // If no message was found, return the empty optional
            return Optional.empty();
        }
        
        // TODO: Use timestamp ByteBuffer to get message timestamp
        return Optional.of(new CANMessage(CANMessageID.fromMessageId(messageIDBuffer.asIntBuffer().get()), messageContent, 0));
        
    }
    
    public static Set<CANDeviceTrace> scanCANDevices (int messagesToScan) {
        
        // Create a set of CAN device traces to return
        HashSet<CANDeviceTrace> devices = new HashSet<>();
        
        // TODO: Send the enumerate message or something else to encourage devices to identify themselves
        
        // Scan through the given number of messages to find all device traces
        for (int i = 0; i < messagesToScan; i ++) {
            readMessage().ifPresent(msg -> {
                
                // TODO: Filter out devices with non-device manufacturers or device types
                
                // Add the device trace to the set
                CANDeviceTrace deviceTrace = new CANDeviceTrace(
                    msg.messageID().deviceType(),
                    msg.messageID().manufacturer(),
                    msg.messageID().deviceNum()
                );
                
                devices.add(deviceTrace);
                
            });
        }
        
        return devices;
        
    }
    
    private CANScanner () { }
    
}
