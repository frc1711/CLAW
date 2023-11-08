package main.java.claw.framework.SwerveFramework;

//This code is meant to be copy-pasted into a robot to speed up the process of programming swerve.
//This code is written to be used in tandem with all other files in the Swerve folder of framework 
//This code assumes that the user has imported Revlib and NavxLib.
//Imports must be done manually
public class SwerveFramework {

    private SwerveModule 
    flModule,
    frModule,
    rlModule,
    rrModule;

  private AHRS gyro;

  private SwerveDriveKinematics kinematics;

  public Swerve(
    SwerveModule flModule,
    SwerveModule frModule,
    SwerveModule rlModule,
    SwerveModule rrModule,
    AHRS gyro
  ) {
    this.flModule = flModule;
    this.frModule = frModule;
    this.rlModule = rlModule;
    this.rrModule = rrModule;
    this.gyro = gyro;
    kinematics = new SwerveDriveKinematics(
        flModule.motorMeters,
        frModule.motorMeters,
        rlModule.motorMeters,
        rrModule.motorMeters
    );
    this.gyro = gyro;

    /**Create a new sendable field for each module*/
    RobotContainer.putSendable("fl-Module", flModule);
    RobotContainer.putSendable("fr-Module", frModule);
    RobotContainer.putSendable("rl-Module", rlModule);
    RobotContainer.putSendable("rr-Module", rrModule); 
    RobotContainer.putSendable("gyro", gyro);

    /**Create a new sendable command to reset the encoders */
    RobotContainer.putCommand("Reset Encoders", new InstantCommand(this::resetEncoders, this), true);
    RobotContainer.putCommand("Reset Gyro", new InstantCommand(this::resetGyro, this), true);
  }

  /**Runs the stop() method on each module */
  public void stop () {
    flModule.stop();
    frModule.stop();
    rlModule.stop();
    rrModule.stop();
  }

  /**Resets the gyros value*/
  public void resetGyro() {
    gyro.reset();
  }

  /**Returns the rotation of the gyro in a Rotation2d*/
  public Rotation2d getGyroRotation () {
    return gyro.getRotation2d();
  }

  /**Returns the pitch of the gyro in degrees.*/
  public float getGyroPitch () {
    return gyro.getPitch();
  }

  /**Runs the resetEncoder() method on each module */
  public void resetEncoders() {
    flModule.resetEncoder();
    frModule.resetEncoder();
    rlModule.resetEncoder();
    rrModule.resetEncoder();
  }

  /**Uses SwerveKinematics to create an array of SwerveModuleStates which are then used to update the individual SwerveModules*/
  public void updateModulesFieldRelative (ChassisSpeeds desiredVelocity, double speedMultiplier) {
    ChassisSpeeds.fromFieldRelativeSpeeds(desiredVelocity, gyro.getRotation2d());
    SwerveModuleState[] moduleStates = kinematics.toSwerveModuleStates(desiredVelocity);
    flModule.update(moduleStates[0], speedMultiplier);
    frModule.update(moduleStates[1], speedMultiplier);
    rlModule.update(moduleStates[2], speedMultiplier);
    rrModule.update(moduleStates[3], speedMultiplier);
  }

  /**Updates each module using the reverse kinematics feature from SwerveDriveKinematics */
  public void updateModules (ChassisSpeeds desiredVelocity, double speedMultiplier) {
    SwerveModuleState[] moduleStates = kinematics.toSwerveModuleStates(desiredVelocity);
    flModule.update(moduleStates[0], speedMultiplier);
    frModule.update(moduleStates[1], speedMultiplier);
    rlModule.update(moduleStates[2], speedMultiplier);
    rrModule.update(moduleStates[3], speedMultiplier);
  }

  /**Theoretically sets the swerve modules to face toward the center of the robot in order to prevent it from moving.*/
  public void xMode () {
    flModule.update(new SwerveModuleState(0, new Rotation2d(135)), 1);
    frModule.update(new SwerveModuleState(0, new Rotation2d(-135)), 1);
    rlModule.update(new SwerveModuleState(0, new Rotation2d(45)), 1);
    rrModule.update(new SwerveModuleState(0, new Rotation2d(-45)), 1);
  }

  @Override
  public void periodic() {
    
  }
}