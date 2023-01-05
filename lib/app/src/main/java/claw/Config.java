package claw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Config {
    
    public static final long serialVersionUID = 2L;
    private static final File CONFIG_FILE = new File("/home/lvuser/claw-config.ser");
    
    private static Config instance = null;
    
    /**
     * The set of all field names used by {@link ConfigField}s.
     */
    private transient Set<String> usedFieldNames;
    
    /**
     * The map of all values obtained from the config file, updated by fields during runtime.
     */
    private final Map<String, Serializable> entries;
    
    private Config () {
        this(new HashMap<String, Serializable>());
    }
    
    private Config (Map<String, Serializable> entries) {
        this.entries = entries;
    }
    
    public static Config getInstance () {
        if (instance == null)
            instance = readConfig();
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private static Config readConfig () {
        // Attempt to open a file input stream
        try (FileInputStream fileInput = new FileInputStream(CONFIG_FILE)) {
                
            // Try to read the config object from the file input stream and set the instance
            ObjectInputStream objIn = new ObjectInputStream(fileInput);
            return new Config(readSerializedEntries((Map<String, byte[]>)objIn.readObject()));
            
        } catch (Exception e) {
            
            // If something went wrong just create a new, empty config object
            return new Config();
            
        }
    }
    
    private static void writeConfig (Map<String, Serializable> entries) throws IOException {
        // Attempt to open a file output stream
        FileOutputStream fileOutput = new FileOutputStream(CONFIG_FILE);
        
        // Try to write the config object to the file output stream
        new ObjectOutputStream(fileOutput).writeObject(writeSerializedEntries(entries));
        
        // Close the output stream
        fileOutput.close();
    }
    
    private static Map<String, Serializable> readSerializedEntries (Map<String, byte[]> dataMap) {
        
        // Create a new serialMap of string names onto serializable objects
        Map<String, Serializable> serialMap = new HashMap<String, Serializable>();
        
        // For each entry in the dataMap, add another entry to the serialMap
        dataMap.forEach((key, bytes) -> {
            
            // Try to deserialize the value from the dataMap
            Serializable obj;
            try {
                obj = (Serializable)(new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
            } catch (Exception e) {
                obj = null;
            }
            
            // Add the object read from the dataMap to the serialMap
            serialMap.put(key, obj);
        });
        
        return serialMap;
        
    }
    
    private static Map<String, byte[]> writeSerializedEntries (Map<String, Serializable> serialMap) {
        
        // Create a new dataMap of string names onto serialized objects
        Map<String, byte[]> dataMap = new HashMap<String, byte[]>();
        
        // For each entry in the serialMap, add another entry to the dataMap
        serialMap.forEach((key, obj) -> {
            try {
                // Try to serialize the object from the serialMap
                ByteArrayOutputStream objBytes = new ByteArrayOutputStream();
                new ObjectOutputStream(objBytes).writeObject(obj);
                
                // Add the object from the serialMap to the dataMap
                dataMap.put(key, objBytes.toByteArray());
            } catch (Exception e) { }
        });
        
        return dataMap;
    }
    
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T getEntry (String name, T defaultValue) {
        if (entries.containsKey(name)) {
            try {
                return (T)entries.get(name);
            } catch (ClassCastException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
    
    private void setEntry (String name, Serializable newValue) {
        entries.put(name, newValue);
    }
    
    public <T extends Serializable> ConfigField<T> getField (String name) throws ConfigNameConflict {
        // Create a new HashSet for usedFieldNames if it is null, because as a transient
        // field it is not read from the config serialization file
        if (usedFieldNames == null)
            usedFieldNames = new HashSet<String>();
        
        // If the field name already exists, throw an exception
        if (usedFieldNames.contains(name))
            throw new ConfigNameConflict(name);
        
        // Add the new field name
        usedFieldNames.add(name);
        
        // Return a new field
        return new ConfigField<T>(name);
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
    
    public void save () throws IOException {
        writeConfig(entries);
    }
    
    public static class ConfigNameConflict extends RuntimeException {
        public ConfigNameConflict (String name) {
            super("The config field '"+name+"' is already in use.");
        }
    }
    
}
