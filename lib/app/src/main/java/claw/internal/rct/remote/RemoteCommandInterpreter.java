package claw.internal.rct.remote;

import java.util.List;
import java.util.Set;

import claw.CLAWRobot;
import claw.internal.Registry;
import claw.CLAWRobot.RobotMode;
import claw.internal.rct.commands.Command;
import claw.internal.rct.commands.CommandLineInterpreter;
import claw.internal.rct.commands.CommandProcessor;
import claw.internal.rct.commands.CommandLineInterpreter.CommandNotRecognizedException;
import claw.internal.rct.commands.CommandProcessor.BadArgumentsException;
import claw.internal.rct.commands.CommandProcessor.CommandFunction;
import claw.internal.rct.network.low.ConsoleManager;
import claw.api.devices.Device;
import claw.api.devices.ConfigBuilder.BadMethodCall;
import claw.api.subsystems.SubsystemCLAW;

public class RemoteCommandInterpreter {
    
    private final CommandLineInterpreter interpreter = new CommandLineInterpreter();
    
    private final Registry<SubsystemCLAW> subsystemRegistry;
    private final Registry<Device<?>> deviceRegistry;
    
    public RemoteCommandInterpreter (Registry<SubsystemCLAW> subsystemRegistry, Registry<Device<?>> deviceRegistry) {
        this.subsystemRegistry = subsystemRegistry;
        this.deviceRegistry = deviceRegistry;
        addCommands();
    }
    
    private void addCommands () {
        
        // TODO: Standardize the help command/description and create a new message class so that a listing of commands can be sent from remote, so nonexistent commands can be caught even if there is no connection to remote
        
        addCommand("ping", "[ping usage]", "[ping help]", this::pingCommand);
        addCommand("test", "[test usage]", "[test help]", this::testCommand);
        addCommand("restart", "[restart usage]", "[restart help]", this::restartCommand);
        addCommand("subsystems", "[subsystems usage]", "[subsystems help]", this::subsystemsCommand);
        addCommand("config", "config", "config", this::configCommand);
        addCommand("devices", "devices", "devices", this::devicesCommand);
    }
    
    private void addCommand (String command, String usage, String helpDescription, CommandFunction function) {
        interpreter.addCommandProcessor(new CommandProcessor(command, usage, helpDescription, function));
    }
    
    public void processLine (ConsoleManager console, String line)
            throws Command.ParseException, BadArgumentsException, CommandNotRecognizedException {
        interpreter.processLine(console, line);
    }
    
    
    
    private void pingCommand (ConsoleManager console, Command cmd) {
        console.println("pong");
        String input = console.readInputLine();
        console.printlnSys("Read input line: " + input);
    }
    
    private void restartCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.checkNumArgs(0, 1, cmd.argsLen());
        
        RobotMode restartMode = RobotMode.DEFAULT;
        
        if (cmd.argsLen() == 1) {
            
            // Restart mode passed in as an argument
            CommandProcessor.expectedOneOf("restart mode", cmd.getArg(0), "default", "sysconfig");
            
            if (cmd.getArg(0).equals("sysconfig")) {
                restartMode = RobotMode.SYSCONFIG;
            }
            
        }
        
        console.println("Restarting...");
        
        CLAWRobot.getInstance().restartCode(restartMode);
    }
    
    private void subsystemsCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.checkNumArgs(1, 3, cmd.argsLen());
        
        String firstArg = cmd.getArg(0);
        
        // TODO: Migrate functionality to new status and config commands (status subsystem SubsystemName, config subsystem SubsystemName)
        
        if (firstArg.equals("list")) {
            
            List<String> subsystemNames = subsystemRegistry.getItemNames();
            if (subsystemNames.size() == 0) {
                console.println("No CLAW subsystems were found.");
            } else {
                for (String name : subsystemNames)
                    console.println(name);
                console.println("count: " + subsystemNames.size());
            }
            
        }
        
    }
    
    private void devicesCommand (ConsoleManager console, Command cmd) throws BadArgumentsException {
        CommandProcessor.checkNumArgs(1, 2, cmd.argsLen());
        
        String operation = cmd.getArg(0);
        
        CommandProcessor.expectedOneOf("operation", operation, "list", "read", "call");
        
        if (operation.equals("list")) {
            
            if (cmd.argsLen() == 1) {
                
                for (Device<?> device : deviceRegistry.getAllItems())
                    console.println(device.getName());
                console.println(deviceRegistry.getSize() + " devices found");
                
            } else {
                
                Device<?> device = getDevice(cmd);
                Set<String> methodNames = device.getMethods();
                Set<String> fieldNames = device.getFields();
                
                if (methodNames.size() == 0) {
                    console.println("No methods");
                } else {
                    console.println("Methods");
                    for (String methodName : device.getMethods())
                        console.println("  "+methodName);
                    console.println("");
                }
                
                if (fieldNames.size() == 0) {
                    console.println("No fields");
                } else {
                    console.println("Fields");
                    for (String fieldName : device.getFields())
                        console.println("  "+fieldName);
                }
            }
            
        } else if (operation.equals("read")) {
            
            Device<?> device = getDevice(cmd);
            console.print("Read value: ");
            String line = console.readInputLine();
            console.println(device.readField(line));
            
        } else if (operation.equals("call")) {
            
            Device<?> device = getDevice(cmd);
            console.print("Set value: ");
            String line = console.readInputLine();
            
            try {
                device.callConfigMethod(line);
            } catch (BadMethodCall e) {
                console.printlnErr(e.getMessage());
            }
            
        }
    }
    
    private Device<?> getDevice (Command cmd) throws BadArgumentsException {
        if (cmd.argsLen() != 2)
            throw new BadArgumentsException("Operation requires a device name.");
        String deviceName = cmd.getArg(1);
        
        if (!deviceRegistry.hasItem(deviceName))
            throw new BadArgumentsException("Device name not found.");
        
        return deviceRegistry.getItem(deviceName);
    }
    
    private void configCommand (ConsoleManager console, Command cmd) {
        
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
    
    private void testCommand (ConsoleManager console, Command cmd) {
        int number = 0;
        
        console.println("");
        while (!console.hasInputReady()) {
            console.moveUp(1);
            number ++;
            console.printlnSys(""+number);
        }
    }
    
}
