package claw;

import java.io.Serializable;
import java.util.HashMap;

import claw.Registry.NameConflictException;

// TODO: Add logger into the LiveUnit structure, add a varargs runnable list to the constructor (getComponent)
// and simplify the config interface to use getStringSetting, getIntSetting, etc.
public class LiveUnit {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.liveUnits");
    
    private final String name;
    
    private final HashMap<String, String> fields = new HashMap<>();
    private final Registry<Setting<?>> settings = new Registry<>("setting");
    
    LiveUnit (String name) {
        this.name = name;
    }
    
    public String getName () {
        return name;
    }
    
    private <T extends Serializable> Setting<T> getSetting (String name) {
        Setting<T> setting = new Setting<>(name);
        
        try {
            settings.add(name, setting);
        } catch (NameConflictException e) {
            // TODO: Make this output a warning to the live unit's log rather than the more abstract claw.liveUnits
            LOG.sublog("settings").out("Warning: " + e.getMessage());
        }
        
        return setting;
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
    
    @SuppressWarnings("unchecked")
    public HashMap<String, String> getFields () {
        return (HashMap<String, String>)fields.clone();
    }
    
}
