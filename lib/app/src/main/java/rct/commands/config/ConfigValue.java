package rct.commands.config;

import java.io.Serializable;

public class ConfigValue implements Serializable {
        
    public final String representation;
    public final Type type;
    
    private ConfigValue (String representation, Type type) {
        this.representation = representation;
        this.type = type;
    }
    
    public static ConfigValue getIntValue (int value) {
        return new ConfigValue(Integer.toString(value), Type.INT);
    }
    
    public static ConfigValue getDoubleValue (double value) {
        return new ConfigValue(Double.toString(value), Type.DOUBLE);
    }
    
    public static ConfigValue getStringValue (String value) {
        return new ConfigValue(value, Type.STRING);
    }
    
    public static ConfigValue getBooleanValue (boolean value) {
        return new ConfigValue(Boolean.toString(value), Type.BOOLEAN);
    }
    
    public static enum Type {
        INT,
        DOUBLE,
        STRING,
        BOOLEAN,
    }
    
}
