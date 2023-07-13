package claw.hardware.encoders;

import edu.wpi.first.wpilibj.Encoder;

public interface QuadEncoderBase {
    
    /**
     * Get the encoder's measured displacement. Units may be arbitrary.
     * @return  The encoder's measured displacement.
     */
    public double getDisplacement ();
    
    /**
     * Get the encoder's measured velocity. This must be measured in units
     * of {@code u/sec}, where {@code u} is the same unit used by {@link #getDisplacement()}.
     * @return  The encoder's measured velocity.
     */
    public double getVelocity ();
    
    public static QuadEncoderBase fromEncoder (Encoder encoder) {
        return new QuadEncoderBase() {
            @Override
            public double getDisplacement () {
                return encoder.getDistance();
            }
            
            @Override
            public double getVelocity () {
                return encoder.getRate();
            }
        };
    }
    
}
