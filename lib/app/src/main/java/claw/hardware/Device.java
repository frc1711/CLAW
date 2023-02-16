package claw.hardware;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import claw.Setting;
import claw.logs.CLAWLogger;

/**
 * A thread-safe wrapper around any hardware components which are connected to the roboRIO on a certain port or with a given ID.
 * The core functionality of the {@code Device} wrapper is initializing a device given an ID. IDs will be read from CLAW settings,
 * so they can be set via the Robot Control Terminal.
 * <br></br>
 * Examples of good components to use with this device wrapper could be resources connected to the roboRIO via CAN, DIO, or PWM (e.g.
 * motor controllers, limit switches, CANCoders, or anything else which has some sort of integer ID).
 */
public class Device <T> {
    
    private static final CLAWLogger LOG = CLAWLogger.getLogger("claw.devices");
    
    /**
     * A map of unique device names onto their set IDs, which is saved to the roboRIO so that the IDs are persistent.
     */
    private static final Setting<HashMap<String, Integer>> DEVICE_NAMES_TO_IDS = new Setting<>("claw.devices", HashMap::new);
    
    /**
     * A lock for the {@code DEVICE_NAMES_TO_IDS} so that the hashmap is not used by more than one thread at once.
     */
    private static final Object DEVICE_MAP_LOCK = new Object();
    
    /**
     * A set of existing device names.
     */
    private static final HashSet<String> allDeviceNames = new HashSet<>();
    
    private final String deviceName;
    private final DeviceInitializer<T> initializer;
    private final DeviceFinalizer<T> finalizer;
    
    /**
     * The currently instantiated device resource (like a motor controller or digital input, for example).
     */
    private Optional<T> device = Optional.empty();
    
    /**
     * A lock for {@code device} so that it isn't used in more than one thread at a time. 
     */
    private final Object deviceLock = new Object();
    
    /**
     * Create a new device wrapper with a given initializer, finalizer, and name.
     * @param deviceName    A unique, user-friendly string which can be used to identify this device.
     * No two {@link Device}s should be initialized with the same {@code deviceName} throughout the runtime
     * of a program. Also, it should be noted that if this name changes for a particular device, you will need
     * to reset its ID through the robot control terminal. It is common to use the format {@code "PORTTYPE.DEVTYPE.SUBSYSTEM.IDENTIFIER"}, where
     * PORTTYPE is the ID system to use (e.g. CAN, DIO, PWM), DEVTYPE is the type of device used (e.g. motor controller, limit switch),
     * SUBSYSTEM is the name of the subsystem the device belongs to, and IDENTIFIER is name which will distinguish the
     * device from other similar devices in the subsystem (based on location or purpose).
     * A sample {@code deviceName} could be {@code "CAN.ENCODER.SWERVE.FRONT_LEFT"}.
     * @param initializer   A {@link DeviceInitializer} which accepts an integer device ID and returns the underlying
     * device object (e.g. a motor controller or digital input). It may be useful to also apply some basic settings to
     * the device in this initializer, such as setting a motor controller's default brake mode to coast, or resetting
     * a relative encoder.
     * @param finalizer     A {@link DeviceFinalizer} which finalizes a device before and puts it in a safe state before
     * the resource is released (e.g. stopping a motor controller). It may be possible that the finalizer will perform no
     * operations on the device.
     */
    public Device (String deviceName, DeviceInitializer<T> initializer, DeviceFinalizer<T> finalizer) {
        
        // Handle the allDeviceNames set
        synchronized (allDeviceNames) {
            // Throw an exception if a device already has the provided name
            if (allDeviceNames.contains(deviceName))
                throw new IllegalArgumentException("A device has already been instantiated with the name '"+deviceName+"'");
            
            // Add the device name to the set
            allDeviceNames.add(deviceName);
        }
        
        // Set instance fields
        this.deviceName = deviceName;
        this.initializer = initializer;
        this.finalizer = finalizer;
    }
    
    /**
     * Get all instantiated devices' names. This may or may not differ from the
     * set of device names associated with a saved ID.
     * @return  The set of all instantiated {@link Device}s' names.
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getAllDeviceNames () {
        return (Set<String>)allDeviceNames.clone();
    }
    
    /**
     * Attempt to save a new integer ID to a particular device. This will be saved to the roboRIO such that the
     * device ID will be persistent between program instances (i.e. if you restart your robot code or turn the robot off,
     * the device ID will still be saved with the given value). Notably, the change in ID will not take effect on any existing
     * {@link Device} instances. In order for devices IDs to be refreshed according to their saved values, use {@link #reinitialize()}.
     * @param deviceName    The device's unique name.
     * @param id            The new device ID.
     * @return              {@code true} if the ID was successfully saved, {@code false} otherwise.
     */
    public static boolean setDeviceID (String deviceName, int id) {
        synchronized (DEVICE_MAP_LOCK) {
            DEVICE_NAMES_TO_IDS.get().put(deviceName, id);
            return DEVICE_NAMES_TO_IDS.save();
        }
    }
    
    /**
     * Get the device ID from the settings
     */
    private int getId () {
        synchronized (DEVICE_MAP_LOCK) {
            // Retrieve the boxed Integer ID from the settings
            Integer id = DEVICE_NAMES_TO_IDS.get().get(deviceName);
            
            // Return the ID
            if (id != null) {
                // If the ID was found in the settings, return it
                return id.intValue();
            } else {
                // If the ID was not found in the settings, return 0 and log a warning
                LOG.out("Warning: No device ID was found for device name '"+deviceName+"'");
                return 0;
            }
        }
    }
    
    /**
     * Initialize a new device according to the ID in settings
     */
    private T initializeDevice () {
        return initializer.initializeDevice(getId());
    }
    
    /**
     * Retrieve the underlying resource belonging to this {@link Device}. If the device resource has already been initialized according
     * to the ID saved to the CLAW settings, then it will not be reinitialized. To reinitialize the device, use {@link #reinitialize()}.
     * @return The underlying resource (e.g. a motor controller or digital input).
     */
    public T get () {
        synchronized (deviceLock) {
            // If the device has not yet been initialized, initialize it
            if (device.isEmpty())
                device = Optional.of(initializeDevice());
            
            // Return the new device
            return device.get();
        }
    }
    
    /**
     * Reinitialize the device resource according to the ID saved to the CLAW settings. This should only be called
     * when the device isn't actively being operated.
     */
    public void reinitialize () {
        synchronized (deviceLock) {
            // Finalize the device if it exists
            if (device.isPresent())
                finalizer.finalizeDevice(device.get());
            
            // Initialize the new device
            device = Optional.of(initializeDevice());
        }
    }
    
    /**
     * Get the unique name associated with this device.
     * @return The device's name.
     */
    public String getName () {
        return deviceName;
    }
    
    /**
     * A functional interface which handles initializing a particular device given an integer ID.
     * Generally, this is something like a device connected to the roboRIO via CAN, DIO, or PWM.
     */
    @FunctionalInterface
    public static interface DeviceInitializer <T> {
        /**
         * Retrieve and initialize a device of type {@code T} given an integer ID.
         * @param id    The ID of the device (potentially a CAN ID, DIO port, etc.)
         * @return      The device.
         */
        public T initializeDevice (int id);
    }
    
    /**
     * A functional interface which handles finalizing a device (i.e. putting it into a safe state
     * and releasing any underlying resources).
     */
    @FunctionalInterface
    public static interface DeviceFinalizer <T> {
        /**
         * Finalize the device such that it is in a safe state and any associated resources are released.
         * @param device    The device to finalize.
         */
        public void finalizeDevice (T device);
    }
    
}
