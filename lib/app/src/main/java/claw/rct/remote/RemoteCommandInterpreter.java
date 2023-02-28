package claw.rct.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import claw.CLAWRobot;
import claw.CLAWRobot.RuntimeMode;
import claw.hardware.Device;
import claw.logs.LogHandler;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.commands.CommandProcessor.CommandFunction;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * This class is meant for CLAW's internal use only.
 */
public class RemoteCommandInterpreter extends CommandLineInterpreter {
    
    public RemoteCommandInterpreter () {
        addCommands();
    }
    
    private void addCommands () {
        addCommand("device",
            "device [ list | set | rm | update ]",
            "device list : List all devices and their IDs.\n" +
            "device set NAME ID : Save a new ID for a particular device NAME.\n" +
            "device rm [ NAME | --all | -a ] : Clear a saved ID for a particular device NAME, " +
                "or clear all saved IDs with '-a' or '--all'.\n" +
            "device update : Update all devices on the robot to use their saved IDs according to 'device list'. " +
                "This may only be done when the robot is disabled.",
            this::deviceCommand);
        addCommand("watch",
            "watch [ --all | --none | log name...]",
            "Use -a or --all to watch all logs. Use -n or --none to watch no logs. " +
            "Use 'watch [name]...' to watch only a set of specific logs.",
            this::watchCommand);
        addCommand("restart",
            "restart | restart [ all | server-only ]", // TODO: Here
            "Use restart to restart the robot code in the current " +
            "Use 'watch [name]...' to watch only a set of specific logs.",
            this::watchCommand);
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
    }
    
    private void restartCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNoOptions();
        reader.allowNoFlags();
        
        if (reader.hasNextArg()) {
            String mode = reader.readArgOneOf("restart mode", "Expected a restart mode: 'server-only' or 'all'.", "server-only", "all");
            reader.noMoreArgs();
            
            if (mode.equals("server-only")) {
                console.println("Restarting with CLAW server only.");
                CLAWRobot.restartCode(RuntimeMode.CLAW_SERVER_ONLY);
            } else {
                console.println("Restarting with CLAW server and robot code.");
                CLAWRobot.restartCode(RuntimeMode.CLAW_SERVER_AND_ROBOT_CODE);
            }
            
        } else {
            reader.noMoreArgs();
            console.println("Restarting robot code.");
            CLAWRobot.restartCode();
        }
    }
    
    private void watchCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowOptions("all", "none");
        reader.allowFlags('a', 'n');
        
        if (reader.getFlag('a') || reader.getOptionMarker("all")) {
            
            // Watch all logs
            LogHandler.getInstance().watchAllLogs();
            
        } else if (reader.getFlag('n') || reader.getOptionMarker("none")) {
            
            // Watch no logs
            LogHandler.getInstance().stopWatchingLogs();
            
        } else {
        
            // Unset all previous log names
            if (reader.hasNextArg())
                LogHandler.getInstance().stopWatchingLogs();
            
            // Continue until there are no more arguments
            while (reader.hasNextArg()) {
                String logName = reader.readArgString("logger name");
                LogHandler.getInstance().watchLogName(logName);
            }
            
        }
        
        // Get a sorted list of log names
        List<String> logNamesList = new ArrayList<>();
        LogHandler.getInstance().getRegisteredLogNames().forEach(logNamesList::add);
        logNamesList.sort(String::compareTo);
        
        // Print the list
        logNamesList.forEach(logName -> {
            boolean watched = LogHandler.getInstance().isWatchingLog(logName);
            char watchedChar = watched ? '#' : ' ';
            console.println(watchedChar + " " + logName);
        });
    }
    
    private void deviceCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        String operation = reader.readArgOneOf(
            "operation",
            "The given operation is invalid. Use 'list', 'set', 'rm', or 'update'.",
            "list", "set", "rm", "update"
        );
        
        if (operation.equals("list")) {
            reader.noMoreArgs();
            reader.allowNoOptions();
            reader.allowNoFlags();
            
            int idColumn = 45;
            
            // Get all instantiated devices' names and sort them alphabetically
            Set<String> deviceNames = Device.getAllDeviceNames();
            ArrayList<String> deviceNamesList = new ArrayList<>(deviceNames);
            deviceNamesList.sort(String::compareTo);
            
            // Message if there are no devices
            if (deviceNamesList.size() == 0) {
                console.println("No instantiated devices.");
            }
            
            // Loop through all devices and list their saved IDs
            for (String deviceName : deviceNamesList) {
                // The device name and spacing before the ID
                String prefix = deviceName + " : ";
                String space = " ".repeat(Math.max(idColumn - prefix.length(), 0));
                
                // String representation of the saved device ID
                Optional<Integer> id = Device.getDeviceId(deviceName);
                String idString = id.isPresent() ? id.get().toString() : "No saved ID";
                
                // Printing a line for the device
                console.println(prefix + space + idString);
            }
            
            // Get the set of all device names which have a saved ID but aren't instantiated
            Map<String, Integer> savedDeviceNamesToIDs = Device.getAllSavedDeviceIDs();
            Set<String> unusedSavedDeviceNames = new HashSet<>(savedDeviceNamesToIDs.keySet());
            unusedSavedDeviceNames.removeIf(deviceNames::contains);
            
            // List any unused device IDs saved to the roboRIO
            if (unusedSavedDeviceNames.size() > 0) {
                console.printlnSys(ConsoleManager.formatMessage(
                    "\nThe following device names and IDs are saved to the roboRIO but " +
                    "have no matching (instantiated) devices in the robot code. " +
                    "Unused device settings can be cleared with 'device rm NAME'. " +
                    "Note that this may happen if you're incorrectly instantiating devices. " +
                    "All devices should be instantiated as soon as the robot program starts."
                ));
                
                for (String deviceName : unusedSavedDeviceNames)
                    console.println(deviceName + " ("+savedDeviceNamesToIDs.get(deviceName)+")");
            }
            
        } else if (operation.equals("set")) {
            
            // Read the device name and ID to save
            String deviceName = reader.readArgString("device name");
            int newID = reader.readArgInt("new ID");
            reader.noMoreArgs();
            reader.allowNoOptions();
            reader.allowNoFlags();
            
            // Save the device name and ID
            boolean success = Device.saveDeviceID(deviceName, Optional.of(newID));
            if (success) {
                console.println("Saved successfully: set device ID.");
            } else {
                console.printlnErr("Error saving: failed to set device ID.");
            }
            
        } else if (operation.equals("rm")) {
            
            // Check if all device IDs should be removed
            reader.allowFlags('a');
            reader.allowOptions("all");
            boolean removeAll = reader.getFlag('a') || reader.getOptionMarker("all");
            
            if (removeAll) {
                // Clear all saved IDs
                reader.noMoreArgs();
                if (Device.clearAllSavedIDs()) {
                    console.println("Saved successfully: cleared all saved device IDs.");
                } else {
                    console.printlnErr("Error saving: failed to clear all device IDs.");
                }
            } else {
                // Get the device name to clear the ID from
                Set<String> savedDeviceNames = Device.getAllSavedDeviceIDs().keySet();
                String deviceName = reader.readArgOneOf(
                    "device name",
                    "Expected the name of a device name associated with a saved ID.",
                    savedDeviceNames
                );
                
                reader.noMoreArgs();
                
                // Clear the device ID from the save
                if (Device.saveDeviceID(deviceName, Optional.empty())) {
                    console.println("Saved successfully: cleared device ID.");
                } else {
                    console.printlnErr("Error saving: failed to clear device ID.");
                }
            }
            
        } else if (operation.equals("update")) {
            
            reader.noMoreArgs();
            reader.allowNoOptions();
            reader.allowNoFlags();
            
            // Ensure the robot is disabled
            if (DriverStation.isEnabled()) {
                console.printlnErr(ConsoleManager.formatMessage(
                    "The robot must be disabled before reinitializing devices. " +
                    "Reinitializing a device while enabled could cause damage to " +
                    "the robot or unexpected behavior."
                ));
                return;
            }
            
            Device.reinitializeAllDevices();
            console.println("Devices have been reinitialized with their saved IDs.");
            
        }
    }
    
}
