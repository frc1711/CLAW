package main.java.claw.framework.SwerveFramework;

//This code is meant to be copy-pasted into a WPILib Subsystem to speed up the process of programming swerve. 
//This code is written to be used in tandem with all other files in the Swerve folder of framework
//This code assumes that the user has imported Revlib.
//Imports must be done manually
public class SwerveModule {
    
    CANCoder encoder;

  CANSparkMax driveMotor, steerMotor;

  Translation2d motorMeters;
  PIDController steerPID = new PIDController(.01, 0, 0);

  double unoptimizedRotation, optimizedRotation, encoderOffset, encoderValue, steerSpeed, driveSpeed, drivePercent;

  private CANSparkMax initializeMotor(int motorID) {
    CANSparkMax motor = new CANSparkMax(motorID, MotorType.kBrushless);
    motor.setIdleMode(IdleMode.kBrake);
    return motor;
  }

  // private CANCoder initializeEncoder (int encoderID) {
  //   return new CANCoder(encoderID);
  // }

  public SwerveModule(int steerMotorID, int driveMotorID, int encoderID, Translation2d motorMeters) {
    encoder = new CANCoder(encoderID);
    encoder.configAbsoluteSensorRange(AbsoluteSensorRange.Signed_PlusMinus180);
    driveMotor = initializeMotor(driveMotorID);
    steerMotor = initializeMotor(steerMotorID);
    steerPID.enableContinuousInput(-180, 180);
    this.motorMeters = motorMeters;
    steerPID.setTolerance(1);
  }

  /** Uses Seavers to set the robot speed. Perhaps one day it will use real units.
   */
  private double maxSpeed = (5500 / 60.) * .1 / 2; //Change this number to increase or decrease speed. Higher values speed up the robot, lower values slow it down.
  private double metersPerSecondToPercentage (double metersPerSecond) {
    return (metersPerSecond / maxSpeed);
  }

  /**Sets the encoderOffset to the current value of the CANcoder. This value is 
   * later used to set a new zero position for the encoder. */
  public void resetEncoder () {
    encoderOffset = encoder.getAbsolutePosition() - 180;
  } 

  double finalAngle, regulatedAngle;
  /**Uses the encoder offset, which is set using the resetEncoders() method, 
   * to determine the current position of the CANcoder */
  public Rotation2d getEncoderRotation () {
    regulatedAngle = encoder.getAbsolutePosition() - encoderOffset;
    if (regulatedAngle < -180) regulatedAngle += 360;
    else if (regulatedAngle > 180) regulatedAngle -= 360;
    return Rotation2d.fromDegrees(regulatedAngle);
  }

  /**Takes in a SwerveModuleState, then uses a PID controller to calculate 
   * approximate values for the steerSpeed and the metersPerSecondToVoltage() 
   * method to calculate the driveVoltage. WIP*/
  public void update (SwerveModuleState desiredState, double speedMultiplier) {

    unoptimizedRotation = desiredState.angle.getDegrees();
    SwerveModuleState optimizedState = SwerveModuleState.optimize(desiredState, getEncoderRotation());
    optimizedRotation = optimizedState.angle.getDegrees();

    double encoderRotation = getEncoderRotation().getDegrees();
    double desiredRotation = optimizedState.angle.getDegrees();
    double angularTolerance = 5.0;

    if ((Math.abs(encoderRotation - desiredRotation) < angularTolerance)) this.steerSpeed = 0;
    else this.steerSpeed = steerPID.calculate(encoderRotation, desiredRotation);

    this.drivePercent = metersPerSecondToPercentage(optimizedState.speedMetersPerSecond * speedMultiplier);
    this.driveSpeed = optimizedState.speedMetersPerSecond;

    driveMotor.set(this.drivePercent);
    steerMotor.set(this.steerSpeed);

  }
  
  public void stop () {
    driveMotor.set(0);
    steerMotor.set(0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  @Override
  public void initSendable (SendableBuilder builder) {
    builder.addDoubleProperty("unop-rotation", () -> unoptimizedRotation, null);
    builder.addDoubleProperty("op-rotation", () -> optimizedRotation, null);
    builder.addDoubleProperty("actual-rotation", () -> getEncoderRotation().getDegrees(), null);
    builder.addDoubleProperty("steer-Speed", () -> steerSpeed, null);
    builder.addDoubleProperty("drive-Speed", () -> driveSpeed, null);
    builder.addDoubleProperty("drive-Voltage", () -> drivePercent, null);
  }
}
