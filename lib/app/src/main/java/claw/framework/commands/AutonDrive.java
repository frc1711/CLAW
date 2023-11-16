package main.java.claw.framework.commands;

//This code is written to be copy-pasted into an existing WPILib Command file. 
//This code is written to be used in tandem with everything in the SwerveFramework folder.
//This code assumes that Revlib has been imported.
//Imports must be done manually.
//In-file imports must be done after code has been pasted.
public class AutonDrive {
    
  Swerve swerveSubsystem;
  Timer timer;
  double timeInSeconds, xSpeedMPS, ySpeedMPS, thetaSpeedMPS;

  public AutonDrive(Swerve swerveSubsystem, double timeInSeconds, double xSpeedMPS, double ySpeedMPS, double thetaSpeedMPS) {
    this.swerveSubsystem = swerveSubsystem;
    this.timer = new Timer();
    this.timeInSeconds = timeInSeconds;
    addRequirements(swerveSubsystem);
  }

  @Override
  public void initialize() {
    /**Restarts the timer which is used to run the auton cycle. */
    timer.restart();
    swerveSubsystem.stop();
  }

  @Override
  public void execute() {
    
    /**Checks if the given time has passed since the timer has been 
     * reset. If not, run the updateModules() method to move the robot*/
    if (!timer.hasElapsed(timeInSeconds)) swerveSubsystem.updateModules(new ChassisSpeeds(xSpeedMPS, ySpeedMPS, thetaSpeedMPS), 1);
    else swerveSubsystem.stop();
  }

  @Override
  public void end(boolean interrupted) {
    swerveSubsystem.stop();
    timer.stop();
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
