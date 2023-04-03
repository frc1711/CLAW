package claw.hardware.swerve;

import java.util.ArrayList;
import java.util.function.Supplier;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.interfaces.Gyro;

public class SwerveDriveBuilder {
    
    private final ArrayList<SwerveModuleBase> swerveModules = new ArrayList<>();
    private Pose2d initialPose = new Pose2d();
    
    public SwerveDriveBuilder () {
        
    }
    
    public SwerveDriveBuilder withModules (SwerveModuleBase... modules) {
        for (SwerveModuleBase module : modules) {
            withModule(module);
        }
        
        return this;
    }
    
    public SwerveDriveBuilder withModule (SwerveModuleBase module) {
        swerveModules.add(module);
        return this;
    }
    
    public SwerveDriveBuilder withInitialPose (Pose2d initialPose) {
        this.initialPose = initialPose;
        return this;
    }
    
    public SwerveDriveHandler build (Gyro gyro) {
        return build(gyro::getRotation2d);
    }
    
    public SwerveDriveHandler build (Supplier<Rotation2d> gyro) {
        return new SwerveDriveHandler(initialPose, gyro, swerveModules.toArray(new SwerveModuleBase[0]));
    }
    
}
