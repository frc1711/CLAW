package claw.hardware;

import java.util.Optional;

import claw.LiveValues;
import claw.rct.base.commands.CommandProcessor;
import claw.rct.base.commands.CommandReader;
import claw.rct.base.commands.CommandProcessor.BadCallException;
import claw.rct.base.console.ConsoleManager;
import claw.rct.base.console.ConsoleManager.TerminalKilledException;
import edu.wpi.first.hal.util.AllocationException;
import edu.wpi.first.wpilibj.DigitalInput;

public class DIOReadCommand {
    
    private static final int DIO_PORTS_TOTAL = 10;
    
    public static final CommandProcessor DIO_RAW_COMMAND_PROCESSOR = new CommandProcessor(
        "dioraw",
        "dioraw",
        "Use 'dioraw' to enumerate DIO ports and list their current values. This may be helpful to distinguish between different " +
        "devices if cables are poorly labeled on the roboRIO, or to quickly tell if a device is functioning properly.",
        DIOReadCommand::dioRawCommandFunction
    );
    
    private static void dioRawCommandFunction (ConsoleManager console, CommandReader reader) throws BadCallException, TerminalKilledException {
        reader.allowNone();
        
        DIOPort[] ports = new DIOPort[DIO_PORTS_TOTAL];
        for (int i = 0; i < ports.length; i ++) {
            ports[i] = new DIOPort(i);
        }
        
        LiveValues values = new LiveValues();
        
        while (!console.hasInputReady()) {
            
            for (int i = 0; i < ports.length; i ++) {
                values.setField("DIO["+i+"]", ports[i].toString());
            }
            
            values.update(console);
            
        }
        
        for (int i = 0; i < ports.length; i ++) {
            ports[i].free();
        }
        
    }
    
    private static class DIOPort {
        
        private Optional<DigitalInput> digitalInput = Optional.empty();
        
        private DIOPort (int port) {
            try {
                digitalInput = Optional.of(new DigitalInput(port));
            } catch (AllocationException e) {
                // Ignore AllocationExceptions
            }
        }
        
        @Override
        public String toString () {
            if (digitalInput.isEmpty()) {
                return "Port unavailable (already allocated)";
            } else {
                return Boolean.toString(digitalInput.get().get());
            }
        }
        
        public void free () {
            digitalInput.ifPresent(DigitalInput::close);
        }
        
    }
    
}
