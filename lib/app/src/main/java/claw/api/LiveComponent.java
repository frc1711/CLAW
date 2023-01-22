package claw.api;

import java.io.Serializable;

import claw.internal.Registry;
import claw.internal.SettingsManager;
import claw.internal.Registry.NameConflictException;

public class LiveComponent {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.liveComponents");
    private static final Registry<LiveComponent> components = new Registry<>("component");
    
    public static LiveComponent getComponent (String name) {
        try {
            components.add(name, new LiveComponent(name));
        } catch (NameConflictException e) {
            LOG.out("Warning: " + e.getMessage());
        }
        
        return components.getItem(name);
    }
    
    private final String name;
    
    private final Registry<LiveField<?>> fields = new Registry<>("live field");
    private final Registry<Setting<?>> settings = new Registry<>("setting");
    
    private LiveComponent (String name) {
        this.name = name;
    }
    
    public String getName () {
        return name;
    }
    
    public <T extends Serializable> Setting<T> getSetting (String name) {
        Setting<T> setting = new Setting<>(name);
        
        try {
            settings.add(name, setting);
        } catch (NameConflictException e) {
            LOG.sublog("settings").out("Warning: " + e.getMessage());
        }
        
        return setting;
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
            return SettingsManager.getInstance().getEntry(LiveComponent.this.name + "/" + this.name, defaultValue);
        }
        
        public void setValue (T value) {
            SettingsManager.getInstance().setEntry(LiveComponent.this.name + "/" + this.name, value);
        }
        
    }
    
    public LiveField<Double> getDoubleLiveField (String name) {
        return getLiveField(name);
    }
    
    public LiveField<Integer> getIntegerLiveField (String name) {
        return getLiveField(name);
    }
    
    public LiveField<Boolean> getBooleanLiveField (String name) {
        return getLiveField(name);
    }
    
    public LiveField<String> getStringLiveField (String name) {
        return getLiveField(name);
    }
    
    private <T> LiveField<T> getLiveField (String name) {
        LiveField<T> field = new LiveField<T>(name);
        
        try {
            fields.add(name, field);
        } catch (NameConflictException e) {
            LOG.sublog("fields").out("Warning: " + e.getMessage());
        }
        
        return field;
    }
    
    public class LiveField <T> {
        
        private final String name;
        private T value;
        
        private LiveField (String name) {
            this.name = name;
        }
        
        public String getName () {
            return name;
        }
        
        public void set (T newValue) {
            this.value = newValue;
        }
        
        public T get () {
            return value;
        }
        
    }
    
}
