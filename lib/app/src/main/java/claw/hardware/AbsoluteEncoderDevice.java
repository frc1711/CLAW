package claw.hardware;

import claw.Setting;
import edu.wpi.first.math.geometry.Rotation2d;

/**
 * A device wrapper for an absolute encoder, allowing for reading from the encoder and configuring its
 * offset from zero.
 */
public class AbsoluteEncoderDevice <T> extends Device<T> {
    
    private final Setting<Double> offsetSetting;
    private final EncoderReader<T> encoderReader;
    
    /**
     * Create a new {@link AbsoluteEncoderDevice}.
     * @param deviceName    The unique identifier for the device.
     * @param initializer   The initializer for the device.
     * @param finalizer     The finalizer for the device.
     * @param offsetSetting A {@link Setting} field which can be used to store the encoder's absolute offset.
     * @param encoderReader A function which can read a {@link Rotation2d} from a provided encoder device.
     * @see Device#Device(String, DeviceInitializer, DeviceFinalizer)
     */
    public AbsoluteEncoderDevice (
        String deviceName,
        DeviceInitializer<T> initializer,
        DeviceFinalizer<T> finalizer,
        Setting<Double> offsetSetting,
        EncoderReader<T> encoderReader
    ) {
        super(deviceName, initializer, finalizer);
        this.offsetSetting = offsetSetting;
        this.encoderReader = encoderReader;
    }
    
    /**
     * An functional interface which reads a rotation from an encoder.
     */
    @FunctionalInterface
    public static interface EncoderReader <T> {
        /**
         * Read a rotation from a given encoder device.
         * @param encoderDevice The encoder which the rotation should be read from.
         * @return              The {@link Rotation2d} representing the encoder's current reading.
         */
        public Rotation2d getRotation (T encoderDevice);
    }
    
    /**
     * Get the absolute rotation of the encoder after accounting for the configured offset.
     * @return A {@link Rotation2d} describing the state of the encoder.
     */
    public Rotation2d getRotationMeasurement () {
        // Reading = Supplier + Offset
        return Rotation2d.fromDegrees(encoderReader.getRotation(get()).getDegrees() + offsetSetting.get());
    }
    
    /**
     * Configures the saved offset so that the reading of the absolute encoder using {@link #getRotationMeasurement()} will,
     * at this position, be equal to the provided {@code targetRotation}.
     * @param targetRotation What this encoder should be reading at the current position.
     */
    public void configureOffset (Rotation2d targetRotation) {
        // Offset = Reading - Supplier
        offsetSetting.set(targetRotation.getDegrees() - encoderReader.getRotation(get()).getDegrees());
    }
    
}
