package claw.api;

import java.io.Serializable;

import claw.internal.ConfigManager;
import claw.internal.ConfigManager.NoFieldException;

// TODO: Move over to various fields objects again and integrate with ConfigManager in only API side
public class CLAWSettings {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.settings");
    
    public static String getString (String fieldName, String defaultValue) {
        return getField(fieldName, defaultValue);
    }
    
    public static void setString (String fieldName, String value) {
        ConfigManager.getInstance().setEntry(fieldName, value);
    }
    
    public static int getInt (String fieldName, int defaultValue) {
        return getField(fieldName, defaultValue);
    }
    
    public static void setInt (String fieldName, int value) {
        ConfigManager.getInstance().setEntry(fieldName, value);
    }
    
    public static boolean getBoolean (String fieldName, boolean defaultValue) {
        return getField(fieldName, defaultValue);
    }
    
    public static void setBoolean (String fieldName, boolean value) {
        ConfigManager.getInstance().setEntry(fieldName, value);
    }
    
    public static double getDouble (String fieldName, double defaultValue) {
        return getField(fieldName, defaultValue);
    }
    
    public static void setDouble (String fieldName, double value) {
        ConfigManager.getInstance().setEntry(fieldName, value);
    }
    
    public static void save () {
        try {
            ConfigManager.getInstance().save();
        } catch (Exception e) {
            LOG.err("Critical exception in saving settings: " + e.getMessage());
        }
    }
    
    private static <T extends Serializable> T getField (String fieldName, T defaultValue) {
        try {
            return ConfigManager.getInstance().getEntry(fieldName);
        } catch (NoFieldException e) {
            LOG.out("Warning: " + e.getMessage());
            return defaultValue;
        }
    }
    
}
