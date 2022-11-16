package rct.commands.config;

import java.util.HashMap;

import rct.low.InstructionMessage;

public class SetConfigInstruction extends InstructionMessage {
    
    public static final long serialVersionUID = 1L;
    
    public final HashMap<String, ConfigValue> configurations;
    
    public SetConfigInstruction (int id, HashMap<String, ConfigValue> configurations) {
        super(id);
        this.configurations = configurations;
    }
    
}
