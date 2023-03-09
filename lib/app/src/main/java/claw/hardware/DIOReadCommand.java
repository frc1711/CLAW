package claw.hardware;

import java.util.Optional;

import claw.rct.commands.CommandProcessor;
import claw.rct.commands.CommandReader;
import claw.rct.commands.CommandProcessor.BadCallException;
import claw.rct.network.low.ConsoleManager;
import edu.wpi.first.hal.DIOJNI;
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
    
    private static void dioRawCommandFunction (ConsoleManager console, CommandReader reader) throws BadCallException {
        reader.allowNone();
        
        DIOPort[] ports = new DIOPort[DIO_PORTS_TOTAL];
        for (int i = 0; i < ports.length; i ++) {
            ports[i] = new DIOPort(i);
        }
        
        // TODO: Automatically updating with DIO values
        for (int i = 0; i < ports.length; i ++) {
            console.println(i + " : " + ports[i]);
        }
        
        for (int i = 0; i < ports.length; i ++) {
            ports[i].free();
        }
        
    }
    
    private static class DIOPort {
        private Optional<DigitalInput> digitalInput = Optional.empty();
        private DIOPort (int port) {
            DIOJNI.checkDIOChannel(port);
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
