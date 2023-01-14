package claw.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import claw.api.CLAWLogger;
import claw.internal.Registry.NameConflictException;

public class Config {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.config");
    private final File configFile;
    
    /**
     * The set of all field names used by {@link ConfigField}s.
     */
    private final Registry<ConfigField<Serializable>> fieldRegistry;
    
    /**
     * The map of all values obtained from the config file, updated by fields during runtime.
     */
    private final Map<String, Serializable> entries;
    
    public Config (File configFile) {
        this.configFile = configFile;
        fieldRegistry = new Registry<>("config field");
        
        LOG.out("Reading CLAW configuration from "+configFile.getName());
        
        ConfigSerial serial;
        try {
            serial = ConfigSerial.readFromFile(configFile);
        } catch (Exception e) {
            serial = new ConfigSerial();
            LOG.err("Critical config error. Config entirely failed to load:\n" + e);
        }
        
        entries = serial.getDeserializedEntries(LOG);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T getEntry (String name, T defaultValue) {
        if (entries.containsKey(name)) {
            try {
                return (T)entries.get(name);
            } catch (ClassCastException e) {
                LOG.out("Warning: Field '"+name+"' exists but is not the expected type (defaulted)");
                entries.put(name, defaultValue);
                return defaultValue;
            }
        } else {
            LOG.out("Warning: Field '"+name+"' could not be found (defaulted)");
            entries.put(name, defaultValue);
            return defaultValue;
        }
    }
    
    private void setEntry (String name, Serializable newValue) {
        entries.put(name, newValue);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Serializable> ConfigField<T> getField (String name) {
        ConfigField<T> field = new ConfigField<T>(name);
        try {
            fieldRegistry.add(name, (ConfigField<Serializable>)field);
        } catch (NameConflictException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
        return field;
    }
    
    public class ConfigField <T extends Serializable> {
        
        private final String name;
        
        private ConfigField (String name) {
            this.name = name;
        }
        
        public String getName () {
            return name;
        }
        
        public T getValue (T defaultValue) {
            return getEntry(name, defaultValue);
        }
        
        public void setValue (T newValue) {
            setEntry(name, newValue);
        }
        
    }
    
    public void save () {
        LOG.out("Saving CLAW configuration to "+configFile.getName());
        ConfigSerial serial = ConfigSerial.fromObjectMap(entries, LOG);
        
        try {
            serial.writeToFile(configFile);
        } catch (Exception e) {
            logCriticalError("CLAW config failed to save", e);
        }
    }
    
    private void logCriticalError (String message, Exception e) {
        LOG.err("Critical config error. "+message+":\n" + e.getMessage());
    }
    
    public Map<String, String> getFields () {
        HashMap<String, String> fields = new HashMap<String, String>();
        
        for (Entry<String, Serializable> entry : entries.entrySet()) {
            String valueString;
            
            if (entry.getValue() == null) valueString = "null";
            else valueString = entry.getValue().toString();
            
            fields.put(entry.getKey(), valueString);
        }
        
        return fields;
    }
    
    /**
     * The {@code Serializable} written to and read from the config file. 
     */
    private static class ConfigSerial implements Serializable {
        
        public static final long serialVersionUID = 1L;
        
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
