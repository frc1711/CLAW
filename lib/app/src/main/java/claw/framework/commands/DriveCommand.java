package main.java.claw.framework.commands;

//This code is written to be copy-pasted into an existing WPILib Command file. 
//This code is written to be used in tandem with everything in the SwerveFramework folder.
//This code assumes that Revlib has been imported.
//Imports must be done manually.
//In-file imports must be done after code has been pasted.
public class DriveCommand {
    
    Swerve swerveSubsystem;

    Timer timer;

    DoubleSupplier xSpeed, ySpeed, thetaSpeed;

    BooleanSupplier slowMode, resetEncoders, resetGyro, xMode;

    public DriveCommand(
        Swerve swerveSubsystem,
        DoubleSupplier xSpeed,
        DoubleSupplier ySpeed,
        DoubleSupplier thetaSpeed,
        BooleanSupplier slowMode,
        BooleanSupplier resetGyro,
        BooleanSupplier xMode
    ) {
        this.swerveSubsystem = swerveSubsystem;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.thetaSpeed = thetaSpeed;
        this.slowMode = slowMode;
        this.resetGyro = resetGyro;
        this.xMode = xMode;

        timer = new Timer();

        addRequirements(swerveSubsystem);
    }

    
    @Override
    public void initialize() {
        swerveSubsystem.stop();
        timer.start();
    }

    double speedMultiplier, oneEighty, turnSpeed;
    boolean wasOneEighty;
    @Override
    public void execute() {

        double transformedXSpeed = xSpeed.getAsDouble();
        double transformedYSpeed = ySpeed.getAsDouble();
        double transformedThetaSpeed = thetaSpeed.getAsDouble();

        double xDeadband = 0.15;
        double yDeadband = 0.15;
        double thetaDeadband = 0.15;

        if (Math.abs(transformedXSpeed) < xDeadband) transformedXSpeed = 0;
        if (Math.abs(transformedYSpeed) < yDeadband) transformedYSpeed = 0;
        if (Math.abs(transformedThetaSpeed) < thetaDeadband) transformedThetaSpeed = 0;

        this.speedMultiplier = slowMode.getAsBoolean() ? 0.25 : 1;

        if (resetGyro.getAsBoolean()) swerveSubsystem.resetGyro();

        if (xMode.getAsBoolean()) swerveSubsystem.xMode();
        else if (
        Math.abs(transformedXSpeed) > .15 ||
        Math.abs(transformedYSpeed) > .15 ||
        Math.abs(transformedThetaSpeed) > .15
        ) {

        this.turnSpeed = transformedThetaSpeed;

        swerveSubsystem.updateModules(
            ChassisSpeeds.fromFieldRelativeSpeeds(
            transformedXSpeed,
            transformedYSpeed,
            this.turnSpeed + oneEighty,
            swerveSubsystem.getGyroRotation()
            ),
            speedMultiplier
        );} 

        else swerveSubsystem.stop();
        

    }

    
    @Override
    public void end(boolean interrupted) {
        swerveSubsystem.stop();
    }

    
    @Override
    public boolean isFinished() {
        return false;
    }
}
