package claw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.function.Supplier;

import claw.logs.CLAWLogger;
import edu.wpi.first.wpilibj.Filesystem;

/**
 * A thread-safe class which handles saving and reading a serializable value from the roboRIO filesystem.
 */
public class Setting <T extends Serializable> {
    
    // TODO: Clearing unused settings
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.settings");
    
    private static final File BASE_CONFIG_DIRECTORY = new File(
        Filesystem.getOperatingDirectory().getAbsolutePath() + File.separator + "config-settings"
    );
    
    private static final Object BASE_CONFIG_DIRECTORY_LOCK = new Object();
    
    /**
     * A set of all the names of settings created
     */
    private final HashSet<String> settingNames = new HashSet<>();
    
    /**
     * Only alphanumeric characters, underscores and periods are allowed
     */
    private static String sanitizeSettingName (String name) {
        return name.replace("[^a-zA-Z0-9_.]", "");
    }
    
    private static File getFileForSetting (String name) {
        if (!BASE_CONFIG_DIRECTORY.exists()) {
            synchronized (BASE_CONFIG_DIRECTORY_LOCK) {
                BASE_CONFIG_DIRECTORY.mkdir();
            }
        }
        
        return new File(BASE_CONFIG_DIRECTORY.getAbsolutePath() + File.separator + name + ".ser");
    }
    
    private static Object readFromFile (File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(file))) {
            return objIn.readObject();
        }
    }
    
    private static void writeToFile (File file, Serializable obj) throws IOException {
        try (ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(file))) {
            objOut.writeObject(obj);
        }
    }
    
    /**
     * Serves as a lock to ensure we don't read from or write to the save file at the same time. This makes
     * the reading and writing operations thread-safe.
     */
    private final Object saveFileLock = new Object();
    
    private final String name;
    private final Supplier<T> defaultValueSupplier;
    
    private boolean hasBeenRead = false;
    private T value = null;
    
    /**
     * Create a new {@link Setting} with the provided name and default value.
     * @param settingName           The (unique) name to use for this setting. This should contain
     * only alphanumeric characters, underscores and periods. {@code settingName} can include other characters,
     * but ultimately it will be sanitized and this will lead to a greater chance of name conflicts
     * between different setting fields.
     * @param defaultValueSupplier  A supplier which can provide this setting with a default value
     * if the setting could not be read from its save file.
     */
    public Setting (String settingName, Supplier<T> defaultValueSupplier) {
        // Sanitize setting name and log a warning if it had to be sanitized
        String sanitizedName = sanitizeSettingName(settingName);
        if (!sanitizedName.equals(settingName))
            LOG.out("Warning: " + settingName + " setting is not valid and has been sanitized to " + sanitizedName);
        
        // Error if the sanitized name is empty (this won't happen often but is still possible)
        if (sanitizedName.isEmpty())
            throw new IllegalArgumentException("After sanitization, the provided setting name '"+settingName+"' was empty");
        
        // Error if the name is already used, and add this setting name to the set of setting names otherwise
        synchronized (settingNames) {
            if (settingNames.contains(sanitizedName))
                throw new IllegalArgumentException("The setting name " + sanitizedName + " is already in use");
            settingNames.add(sanitizedName);
        }
        
        // Set the instance fields
        this.name = sanitizedName;
        this.defaultValueSupplier = defaultValueSupplier;
    }
    
    /**
     * Retrieve this setting's value.
     * @return The value of this setting.
     */
    public T get () {
        if (!hasBeenRead) {
            value = readFromSave();
            hasBeenRead = true;
        } return value;
    }
    
    /**
     * Set and save this setting's value.
     * @param newValue  The value to which this setting should be set.
     * @return          {@code true} if the value was saved successfully, {@code false} otherwise.
     */
    public boolean set (T newValue) {
        value = newValue;
        return save();
    }
    
    /**
     * Save the setting's current value. This is can be useful if the value of this {@link Setting}
     * may have changed its internal state since the last time it was set or saved.
     * @return {@code true} if the value was saved successfully, {@code false} otherwise.
     */
    public boolean save () {
        return writeToSave();
    }
    
    // Reading from and writing to the save file
    
    @SuppressWarnings("unchecked")
    private T readFromSave () {
        // Attempt to read (any) object from this setting's save file
        Object value;
        try {
            // Synchronize with the saveFileLock to prevent IO conflicts
            synchronized (saveFileLock) {
                value = readFromFile(getFileForSetting(name));
            }
        } catch (Exception e) {
            // Log a warning if the setting could not be found, and return the default value
            LOG.out("Warning: Cannot find saved setting '"+name+"', falling back to default value");
            return defaultValueSupplier.get();
        }
        
        // Attempt to cast the read object to the correct type
        try {
            return (T)value;
        } catch (ClassCastException e) {
            // Log a warning if the setting's value could not be casted to the correct type
            LOG.out("Warning: Value for saved setting '"+name+"' has an incompatible type, falling back to default value");
            return defaultValueSupplier.get();
        }
    }
    
    /**
     * @return {@code true} if the setting's current value was saved successfully, {@code false} otherwise
     */
    private boolean writeToSave () {
        try {
            // Synchronize with the saveFileLock to prevent IO conflicts
            synchronized (saveFileLock) {
                writeToFile(getFileForSetting(name), value);
            }
            return true;
        } catch (IOException e) {
            // Log an error if the value could not be saved
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            LOG.err("Failed to save value to setting '"+name+"':\n" + stackTrace.toString());
            return false;
        }
    }
    
}
