package claw.rct.remote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import claw.hardware.Device;
import claw.logs.LogHandler;
import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.commands.CommandProcessor.CommandFunction;
import claw.rct.network.low.ConsoleManager;

/**
 * This class is meant for CLAW's internal use only.
 */
public class RemoteCommandInterpreter extends CommandLineInterpreter {
    
    public RemoteCommandInterpreter () {
        addCommands();
    }
    
    private void addCommands () {
        addCommand("device",
            "device list, device set NAME ID, device rm [ NAME | --all | -a ]",
            "Use 'device list' to list all devices. Use 'device set NAME ID' to set " +
            "the ID for a device NAME. Use 'device rm NAME' to clear a device with a given " +
            "name from the save file, or 'device rm --all' to clear all saved device IDs.",
            this::deviceCommand);
        addCommand("config", "config", "config", this::configCommand);
        addCommand("watch",
            "watch [ --all | --none | log name...]",
            "Use -a or --all to watch all logs. Use -n or --none to watch no logs. " +
            "Use 'watch [name]...' to watch only a set of specific logs.",
            this::watchCommand);
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
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
        String operation = reader.readArgOneOf("operation", "The given operation is invalid. Use 'list', 'set' or 'rm'.", "list", "set", "rm");
        
        if (operation.equals("list")) {
            reader.noMoreArgs();
            reader.allowNoOptions();
            reader.allowNoFlags();
            
            int idColumn = 30;
            
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
            if (success)
                console.println("Saved successfully.");
            else
                console.printlnErr("Error saving new device ID.");
            
        } else if (operation.equals("rm")) {
            
            // Check if all device IDs should be removed
            reader.allowFlags('a');
            reader.allowOptions("all");
            boolean removeAll = reader.getFlag('a') || reader.getOptionMarker("all");
            
            if (removeAll) {
                // Clear all saved IDs
                reader.noMoreArgs();
                Device.clearAllSavedIDs();
            } else {
                // Get the device name to clear the ID from
                String deviceName = reader.readArgString("device name");
                reader.noMoreArgs();
                
                // Clear the device ID from the save
                Device.saveDeviceID(deviceName, Optional.empty());
            }
            
        }
    }
    
    private void configCommand (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        
        
        // TODO: Fix config command
        
        // Map<String, String> fields = Config.getInstance().getFields();
        // for (Entry<String, String> field : fields.entrySet()) {
        //     String limitKey = field.getKey();
        //     if (limitKey.length() > 25)
        //         limitKey = limitKey.substring(0, 25) + "...";
            
        //     console.println(limitKey + " : " + " ".repeat(30 - limitKey.length()) + field.getValue());
        // }
        
        // console.println(fields.size() + " fields");
    }
    
}
