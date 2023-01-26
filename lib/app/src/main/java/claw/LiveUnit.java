package claw;

import java.util.HashMap;

public class LiveUnit {
    
    private final String name;
    
    private final HashMap<String, String> fields = new HashMap<>();
    
    public String getName () {
        return name;
    }
    
    public void put (String fieldName, String value) {
        fields.put(fieldName, value);
    }
    
    public void put (String fieldName, double value) {
        fields.put(fieldName, Double.toString(value));
    }
    
    public void put (String fieldName, int value) {
        fields.put(fieldName, Integer.toString(value));
    }
    
    public void put (String fieldName, boolean value) {
        fields.put(fieldName, Boolean.toString(value));
    }
    
    public void put (String fieldName, Object value) {
        fields.put(fieldName, value.toString());
    }
    
    LiveUnit (String name) {
        this.name = name;
    }
    
    HashMap<String, String> getFields () {
        return fields;
    }
    
}
