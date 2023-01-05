package claw;

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

public class Config implements Serializable {
    
    public static final long serialVersionUID = 1L;
    private static final File CONFIG_FILE = new File("/home/lvuser/claw-config.ser");
    
    private static Config instance = null;
    
    /**
     * The set of all field names used by {@link ConfigField}s.
     */
    private transient Set<String> usedFieldNames;
    
    /**
     * The map of all value obtained from the config file, updated by fields during runtime.
     */
    private final Map<String, Serializable> entries = new HashMap<String, Serializable>();
    
    private Config () { }
    
    public static Config getInstance () {
        
        // If instance is null, set it to a new config object read from the config file
        if (instance == null) {
            
            // TODO: Have config not be deserialized from a single file but instead use a folder so all fields can be serialized separately and one bad field will not take down the config
            
            // Attempt to open a file input stream
            try (FileInputStream fileInput = new FileInputStream(CONFIG_FILE)) {
                
                // Try to read the config object from the file input stream and set the instance
                ObjectInputStream objIn = new ObjectInputStream(fileInput);
                instance = (Config)objIn.readObject();
                
            } catch (Exception e) {
                
                // If something went wrong just create a new, empty config object
                instance = new Config();
                
            }
            
        }
        
        return instance;
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
        // Attempt to open a file output stream
        FileOutputStream fileOutput = new FileOutputStream(CONFIG_FILE);
        
        // Try to write the config object to the file output stream
        new ObjectOutputStream(fileOutput).writeObject(this);
        
        // Close the output stream
        fileOutput.close();
    }
    
    public static class ConfigNameConflict extends RuntimeException {
        public ConfigNameConflict (String name) {
            super("The config field '"+name+"' is already in use.");
        }
    }
    
}
