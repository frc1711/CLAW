package claw.hardware.can;

/**
 * Last updated 2023
 * https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html
 */
public record CANMessageID (byte deviceNum, byte apiIndex, byte apiClass, ManufacturerCode manufacturer, DeviceType deviceType) {
    
    /**
     * Parse a {@link CANMessageID} from an integer message ID.
     * @param fullArbitrationId The full message ID / arbitration ID.
     * @return  The parsed {@code CANMessageID}.
     */
    public static CANMessageID fromMessageId (int fullArbitrationId) {
        
        // Break apart the arbitration ID into its components:
        int[] bitSliceSizes = new int[] {
            6,  // Device number
            4,  // API Index
            6,  // API Class
            8,  // Manufacturer Code
            5,  // Device Type
        };
        
        // Get the binary slices of the message ID corresponding with each useful segment (device number, api index, etc.)
        int[] bitSlices = getBinarySlices(fullArbitrationId, bitSliceSizes);
        
        // Get the byte fields
        byte deviceNum =    (byte)bitSlices[0];
        byte apiIndex =     (byte)bitSlices[1];
        byte apiClass =     (byte)bitSlices[2];
        
        // Parse a ManufacturerCode from bitSlices[3]
        ManufacturerCode manufacturer = getEnumFromId(
            ManufacturerCode.class,
            bitSlices[3],
            ManufacturerCode.UNKNOWN
        );
        
        // Parse a DeviceType from bitSlices[4]
        DeviceType deviceType = getEnumFromId(
            DeviceType.class,
            bitSlices[4],
            DeviceType.UNKNOWN
        );
        
        return new CANMessageID(deviceNum, apiIndex, apiClass, manufacturer, deviceType);
        
    }
    
    /**
     * Get an array of binary slices from a value.
     * @param value         The value to take binary slices from
     * @param sliceSizes    The size of each slice. {@code slice[0]} will be taken from the far right of the value.
     * @return              An array of binary slices corresponding with the given {@code sliceSizes} array
     */
    private static int[] getBinarySlices (int value, int[] sliceSizes) {
        int[] bitSlices = new int[sliceSizes.length];
        
        int startIndex = 0;
        for (int i = 0; i < bitSlices.length; i ++) {
            bitSlices[i] = getBinarySliceFrom(value, startIndex, sliceSizes[i]);
            startIndex += sliceSizes[i];
        }
        
        return bitSlices;
    }
    
    /**
     * Gets a binary slice from a given value.
     * @param value         The value to retrieve the binary slice from.
     * @param startIndex    The index of the first bit to retrieve, starting at the 1's place (index 0).
     * @param length        The number of bits to retrieve from the given {@code value}.
     * @return              The section of bits retrieved from the {@code value}, shifted so the 1's place
     * contains the rightmost bit in the slice.
     */
    private static int getBinarySliceFrom (int value, int startIndex, int length) {
        // This creates an int bitMask which, when viewed in binary representation,
        // looks like 1111110000, where the parameter "length" describes the number of
        // 1 bits, and the parameter "startIndex" describes the number of 0 bits
        int bitMask = ~(-1 << length) << startIndex;
        
        // The bitMask can be ANDed with "value" to get only the bits we want from it.
        // We shift the bits back to the right to undo the "startIndex"
        int maskedNum = (bitMask & value) >>> startIndex;
        
        return maskedNum;
    }
    
    /**
     * Internal, helper interface. Useful for finding enum options that correspond with some given integer ID
     */
    private interface EnumWithID {
        public int getMinId ();
        public int getMaxId ();
    }
    
    /**
     * Helps to find an enum option which corresponds with some given integer ID
     */
    private static <T extends EnumWithID> T getEnumFromId (Class<T> enumClass, int id, T defaultOption) {
        T[] enumOptions = enumClass.getEnumConstants();
        for (int i = 0; i < enumOptions.length; i ++) {
            if (id >= enumOptions[i].getMinId() && id <= enumOptions[i].getMaxId()) {
                return enumOptions[i];
            }
        }
        
        return defaultOption;
    }
    
    /**
     * A named device type according to the FRC CAN Device Specifications.
     */
    public static enum DeviceType implements EnumWithID {
        BROADCAST_MESSAGES          (0),
        ROBOT_CONTROLLER            (1),
        MOTOR_CONTROLLER            (2),
        RELAY_CONTROLLER            (3),
        GYRO_SENSOR                 (4),
        ACCELEROMETER               (5),
        ULTRASONIC_SENSOR           (6),
        GEAR_TOOTH_SENSOR           (7),
        POWER_DISTRIBUTION_MODULE   (8),
        PNEUMATICS_CONTROLLER       (9),
        MISCELLANEOUS               (10),
        IO_BREAKOUT                 (11),
        RESERVED                    (12, 30),
        FIRMWARE_UPDATE             (31),
        UNKNOWN                     (-1);
        
        @Override
        public int getMinId () {
            return minId;
        }
        
        @Override
        public int getMaxId () {
            return maxId;
        }
        
        private final int minId, maxId;
        private DeviceType (int num) {
            minId = num;
            maxId = num;
        }
        
        private DeviceType (int minId, int maxId) {
            this.minId = minId;
            this.maxId = maxId;
        }
    }
    
    /**
     * A manufacturer according to the FRC CAN Device Specifications.
     */
    public static enum ManufacturerCode implements EnumWithID {
        BROADCAST (
            "[Broadcast]",
            0
        ),
        
        NI (
            "National Instruments",
            1
        ),
        
        LUMINARY_MICRO (
            "Luminary Micro",
            2
        ),
        
        DEKA (
            "DEKA Research and Development",
            3
        ),
        
        CTR_ELECTRONICS (
            "Cross the Road Electronics",
            4
        ),
        
        REV_ROBOTICS (
            "REV Robotics",
            5
        ),
        
        GRAPPLE (
            "Grapple",
            6
        ),
        
        MIND_SENSORS (
            "MindSensors",
            7
        ),
        
        TEAM_USE (
            "[Team Use]",
            8
        ),
        
        KAUAI_LABS (
            "Kauai Labs",
            9
        ),
        
        COPPERFORGE (
            "Copperforge",
            10
        ),
        
        PLAYING_WITH_FUSION (
            "Playing With Fusion",
            11
        ),
        
        STUDICA (
            "Studica",
            12
        ),
        
        THE_THRIFTY_BOT (
            "The Thrifty Bot",
            13
        ),
        
        RESERVED (
            "[Reserved]",
            14, 255
        ),
        
        UNKNOWN (
            "[Unknown]",
            -1
        );
        
        @Override
        public int getMinId () {
            return minId;
        }
        
        @Override
        public int getMaxId () {
            return maxId;
        }
        
        private final int minId, maxId;
        
        /**
         * A more readable name for the device type than the enum name.
         */
        public final String friendlyName;
        private ManufacturerCode (String name, int num) {
            this.friendlyName = name;
            minId = num;
            maxId = num;
        }
        
        private ManufacturerCode (String name, int minId, int maxId) {
            this.friendlyName = name;
            this.minId = minId;
            this.maxId = maxId;
        }
    }
    
}
