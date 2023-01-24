package claw;

import java.io.Serializable;
import java.util.HashMap;

public class LiveUnit {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.liveUnits");
    
    private final String name;
    
    private final HashMap<String, String> fields = new HashMap<>();
    private final HashMap<String, Setting<?>> settings = new HashMap<>();
    
    // Public methods
    
    public String getName () {
        return name;
    }
    
    public Setting<String> getStringSetting (String name) {
        return getSetting(name);
    }
    
    public Setting<Double> getDoubleSetting (String name) {
        return getSetting(name);
    }
    
    public Setting<Integer> getIntSetting (String name) {
        return getSetting(name);
    }
    
    public Setting<Boolean> getBooleanSetting (String name) {
        return getSetting(name);
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
    
    public class Setting <T extends Serializable> {
        
        private final String name;
        
        private Setting (String name) {
            this.name = name;
        }
        
        public String getName () {
            return name;
        }
        
        public T getValue (T defaultValue) {
            return SettingsManager.getInstance().getEntry(LiveUnit.this.name + "/" + this.name, defaultValue);
        }
        
        public void setValue (T value) {
            SettingsManager.getInstance().setEntry(LiveUnit.this.name + "/" + this.name, value);
        }
        
    }
    
    // Package-accessible methods
    
    LiveUnit (String name) {
        this.name = name;
    }
    
    HashMap<String, String> getFields () {
        return fields;
    }
    
    HashMap<String, Setting<?>> getSettings () {
        return settings;
    }
    
    // Private methods
    
    private <T extends Serializable> Setting<T> getSetting (String name) {
        Setting<T> setting = new Setting<>(name);
        
        // TODO: Make this output a warning to the live unit's log rather than the more abstract claw.liveUnits
        if (settings.containsKey(name))
            LOG.sublog("settings").out("Warning: The setting '"+name+"' already exists for the live unit '"+name+"'.");
        settings.put(name, setting);
        return setting;
    }
    
}
