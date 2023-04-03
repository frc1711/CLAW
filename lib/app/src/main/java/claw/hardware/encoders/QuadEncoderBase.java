package claw.hardware.encoders;

import edu.wpi.first.wpilibj.Encoder;

public interface QuadEncoderBase {
    public double getDisplacement ();
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
