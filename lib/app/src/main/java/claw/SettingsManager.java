package claw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SettingsManager {
    
    private static final File CONFIG_FILE = new File("/home/lvuser/claw-config.ser");
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.settings");
    
    // Singleton handling
    
    private static SettingsManager instance = null;
    
    private static SettingsManager getInstance () {
        if (instance == null)
            instance = new SettingsManager();
        return instance;
    }
    
    // Low-level settings serialization
    
    /**
     * The map of all values obtained from the config file
     */
    private final Map<String, Serializable> entries;
    
    /**
     * A set of all the names of {@link Setting}s created
     */
    private final HashSet<String> instantiatedSettingsNames = new HashSet<>();
    
    /**
     * Instantiates a new SettingsManager, reading the settings from a file on the roboRIO.
     */
    private SettingsManager () {
        LOG.out("Reading CLAW settings from "+CONFIG_FILE.getName());
        
        ConfigSerial serial;
        try {
            serial = ConfigSerial.readFromFile(CONFIG_FILE);
        } catch (Exception e) {
            serial = new ConfigSerial();
            LOG.err("Critical config error. Config entirely failed to load:\n" + e);
        }
        
        entries = serial.getDeserializedEntries(LOG);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T getEntry (String name, T defaultValue) {
        try {
            if (entries.containsKey(name))
                return (T)entries.get(name);
        } catch (ClassCastException e) { }
        
        LOG.out("Warning: Field '"+name+"' could not be found or was the incorrect type");
        return defaultValue;
    }
    
    private void setEntry (String name, Serializable newValue) {
        entries.put(name, newValue);
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
            return getEntry(name, defaultValue);
        }
        
        public void setValue (T value) {
            setEntry(name, value);
        }
        
    }
    
    public static <T extends Serializable> Setting<T> getSetting (String name) {
        // Log a warning if this setting has already been instantiated
        if (getInstance().instantiatedSettingsNames.contains(name))
            LOG.out("Warning: The setting '"+name+"' was instantiated more than once.");
        getInstance().instantiatedSettingsNames.add(name);
        
        return getInstance().new Setting<>(name);
    }
    
    /**
     * Attempts to save the current state of the settings, returning a boolean indicating whether or
     * not saving was successful.
     * @return {@code true} if saving was successful, {@code false} otherwise.
     */
    public static boolean save () {
        ConfigSerial serial = ConfigSerial.fromObjectMap(getInstance().entries, LOG);
        try {
            serial.writeToFile(CONFIG_FILE);
            return true;
        } catch (Exception e) {
            LOG.err("Critical exception in saving settings: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * The {@code Serializable} written to and read from the config file. 
     */
    private static class ConfigSerial implements Serializable {
        
        public static final long serialVersionUID = 3L;
        
        private final Map<String, byte[]> serializedObjMap;
        
        public ConfigSerial () {
            this(new HashMap<String, byte[]>());
        }
        
        public ConfigSerial (Map<String, byte[]> serializedObjMap) {
            this.serializedObjMap = serializedObjMap;
        }
        
        public static ConfigSerial readFromFile (File file) throws Exception {
            // Try to get an object input stream from the file
            ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(file));
            ConfigSerial serial = (ConfigSerial)(objIn.readObject());
            objIn.close();
            return serial;
        }
        
        public static ConfigSerial fromObjectMap (Map<String, Serializable> objMap, CLAWLogger log) {
            
            // Create a new serialized object map of string names onto raw bytes
            Map<String, byte[]> serializedObjMap = new HashMap<String, byte[]>();
            
            // Serialize each value in the object map and add to the serialized object map
            objMap.forEach((key, obj) -> {
                
                try {
                    
                    // Try to serialize the object from the object map
                    ByteArrayOutputStream objBytes = new ByteArrayOutputStream();
                    new ObjectOutputStream(objBytes).writeObject(obj);
                    
                    // Add the serialized bytes to the serialized object map
                    serializedObjMap.put(key, objBytes.toByteArray());
                    
                } catch (Exception e) {
                    log.err("Failed to write serializable field '" + key + "'");
                }
            });
            
            return new ConfigSerial(serializedObjMap);
        }
        
        public void writeToFile (File file) throws Exception {
            // Attempt to open a file output stream
            FileOutputStream fileOutput = new FileOutputStream(file);
            
            // Try to write the config object to the file output stream
            ObjectOutputStream objOut = new ObjectOutputStream(fileOutput);
            objOut.writeObject(this);
            objOut.close();
        }
        
        public Map<String, Serializable> getDeserializedEntries (CLAWLogger log) {
            
            // Create a new map of object names onto serializable objects
            Map<String, Serializable> objMap = new HashMap<String, Serializable>();
            
            // For each entry in the serialized object map, add another entry to the serialMap
            serializedObjMap.forEach((key, bytes) -> {
                
                // Try to deserialize the bytes into an object
                Serializable obj;
                
                try {
                    ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    obj = (Serializable)(objIn.readObject());
                } catch (Exception e) {
                    log.err("Failed to deserialize field '" + key + "'");
                    obj = null;
                }
                
                // Add the deserialized object to the object map
                objMap.put(key, obj);
            });
            
            return objMap;
        }
        
    }
    
}
