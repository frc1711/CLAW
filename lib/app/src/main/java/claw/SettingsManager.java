package claw;

import java.util.HashSet;
import java.util.Set;

import edu.wpi.first.wpilibj.Preferences;

public class SettingsManager {
    
    private static final String PREF_PREFIX = "CLAW_SETTING#";
    private static final Set<String> usedSettingsNames = new HashSet<String>();
    
    public static BooleanSettingsField getBooleanSettingsField (String name, Boolean defaultValue) throws NameConflict {
        addFieldName(name);
        return new BooleanSettingsField(name, defaultValue);
    }
    
    public static DoubleSettingsField getDoubleSettingsField (String name, Double defaultValue) throws NameConflict {
        addFieldName(name);
        return new DoubleSettingsField(name, defaultValue);
    }
    
    public static IntegerSettingsField getIntegerSettingsField (String name, Integer defaultValue) throws NameConflict {
        addFieldName(name);
        return new IntegerSettingsField(name, defaultValue);
    }
    
    private static void addFieldName (String name) throws NameConflict {
        if (usedSettingsNames.contains(name))
            throw new NameConflict(name);
        usedSettingsNames.add(name);
    }
    
    private static abstract class SettingsField <T> {
        private final String name;
        private final T defaultValue;
        
        public SettingsField (String name, T defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }
        
        public abstract T getValue ();
        public abstract void setValue (T value);
        
        public String getName () {
            return name;
        }
        
        public T getDefault () {
            return defaultValue;
        }
        
    }
    
    public static class BooleanSettingsField extends SettingsField<Boolean> {
        private BooleanSettingsField (String name, Boolean defaultValue) {
            super(name, defaultValue);
        }
        public Boolean getValue () {
            return Preferences.getBoolean(PREF_PREFIX+getName(), getDefault());
        }
        public void setValue (Boolean value) {
            Preferences.setBoolean(PREF_PREFIX+getName(), value);
        }
    }
    
    public static class DoubleSettingsField extends SettingsField<Double> {
        private DoubleSettingsField (String name, Double defaultValue) {
            super(name, defaultValue);
        }
        public Double getValue () {
            return Preferences.getDouble(PREF_PREFIX+getName(), getDefault());
        }
        public void setValue (Double value) {
            Preferences.setDouble(PREF_PREFIX+getName(), value);
        }
    }
    
    public static class IntegerSettingsField extends SettingsField<Integer> {
        private IntegerSettingsField (String name, Integer defaultValue) {
            super(name, defaultValue);
        }
        public Integer getValue () {
            return Preferences.getInt(PREF_PREFIX+getName(), getDefault());
        }
        public void setValue (Integer value) {
            Preferences.setInt(PREF_PREFIX+getName(), value);
        }
    }
    
    public static class NameConflict extends RuntimeException {
        public NameConflict (String name) {
            super("The settings field '"+name+"' is already in use.");
        }
    }
    
}
