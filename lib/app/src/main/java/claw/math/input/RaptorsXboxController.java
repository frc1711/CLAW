package claw.math.input;

import java.util.Optional;

import claw.math.Transform;
import claw.math.Vector;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

/**
 * An extension of the {@link XboxController} providing better control over the D-pad, {@link InputTransform}
 * applications to joysticks, and other quality-of-life improvements.
 */
public class RaptorsXboxController extends XboxController {
    
    private final Transform leftStickTransform, rightStickTransform;
    
    /**
     * Create a new {@link RaptorsXboxController}.
     * @param port The port index of this Xbox controller on the Driver Station.
     */
    public RaptorsXboxController (int port) {
        this(port, Transform.clamp(-1, 1));
    }
    
    /**
     * Create a new {@link RaptorsXboxController} with a {@link Transform} to apply to both joysticks when retrieving
     * their positions as vectors.
     * @param port                  The port index of this Xbox controller on the Driver Station.
     * @param stickVectorTransform  The transform to apply to both joystick vectors' magnitudes. This is recommended
     * to be an {@link InputTransform}.
     */
    public RaptorsXboxController (int port, Transform stickVectorTransform) {
        this(port, stickVectorTransform, stickVectorTransform);
    }
    
    /**
     * Create a new {@link RaptorsXboxController} with {@link Transform}s to apply to the left and right joysticks when retrieving
     * their positions as vectors. The given {@code Transform}s are recommended to be {@link InputTransform}s.
     * @param port                      The port index of this Xbox controller on the Driver Station.
     * @param leftStickVectorTransform  The transform to apply to the left joystick's position vector (magnitude).
     * @param rightStickVectorTransform The transform to apply to the right joystick's position vector (magnitude).
     */
    public RaptorsXboxController (int port, Transform leftStickVectorTransform, Transform rightStickVectorTransform) {
        super(port);
        leftStickTransform = leftStickVectorTransform;
        rightStickTransform = rightStickVectorTransform;
    }
    
    private Vector<N2> getStickVector (double x, double y, Transform transform) {
        // Invert y because the input from the driver station is also inverted
        Vector<N2> rawVector = Vector.from(x, -y);
        
        // Re-scale the vector to a new magnitude
        double rawMagnitude = rawVector.getMagnitude();
        return rawVector.scaleToMagnitude(transform.apply(rawMagnitude));
    }
    
    /**
     * Retrieves the left joystick's position as a {@link Vector}. The left joystick transform
     * given through the constructor method will be applied to the vector's magnitude,
     * if one was provided. Also, the vector will be normalized so that up on the joystick corresponds
     * with a positive y value.
     * @return  A {@code Vector} representing the left joystick's position.
     */
    public Vector<N2> getLeftStickAsVector () {
        return getStickVector(getLeftX(), getLeftY(), leftStickTransform);
    }
    
    /**
     * Retrieves the right joystick's position as a {@link Vector}. The right joystick transform
     * given through the constructor method will be applied to the vector's magnitude,
     * if one was provided. Also, the vector will be normalized so that up on the joystick corresponds
     * with a positive y value.
     * @return  A {@code Vector} representing the right joystick's position.
     */
    public Vector<N2> getRightStickAsVector () {
        return getStickVector(getRightX(), getRightY(), rightStickTransform);
    }
    
    /**
     * Set the controller's rumble.
     * @param leftRumble    A value on the interval [0, 1] representing the rumble to apply to the left side of the controller.
     * @param rightRumble   A value on the interval [0, 1] representing the rumble to apply to the right side of the controller.
     */
    public void setRumble (double leftRumble, double rightRumble) {
        setRumble(RumbleType.kLeftRumble, leftRumble);
        setRumble(RumbleType.kRightRumble, rightRumble);
    }
    
    /**
     * Set the controller's rumble.
     * @param rumbleValue A value on the interval [0, 1] representing the rumble to apply to the controller.
     */
    public void setRumble (double rumbleValue) {
        setRumble(rumbleValue, rumbleValue);
    }
    
    /**
     * Stop all controller rumble.
     */
    public void stopRumble () {
        setRumble(0);
    }
    
    /**
     * Set the controller to rumble for a given duration.
     * @param durationSecs  The duration of the rumble, in seconds.
     * @param leftRumble    A value on the interval [0, 1] representing the rumble to apply to the left side of the controller.
     * @param rightRumble   A value on the interval [0, 1] representing the rumble to apply to the right side of the controller.
     */
    public void setTimedRumble (double durationSecs, double leftRumble, double rightRumble) {
        new SequentialCommandGroup(
            new InstantCommand(() -> this.setRumble(leftRumble, rightRumble)),
            new WaitCommand(durationSecs),
            new InstantCommand(this::stopRumble)
        ).schedule();
    }
    
    /**
     * Set the controller to rumble for a given duration.
     * @param durationSecs  The duration of the rumble, in seconds.
     * @param rumbleValue   A value on the interval [0, 1] representing the rumble to apply to the controller.
     */
    public void setTimedRumble (double durationSecs, double rumbleValue) {
        setTimedRumble(durationSecs, rumbleValue, rumbleValue);
    }
    
    /**
     * Get the {@link DPadDirection} currently being pressed (if one is being pressed).
     * @return  An {@link Optional} containing the pressed {@code DPadDirection}.
     */
    public Optional<DPadDirection> getDPad () {
        
        // Get the DPad direction as an integer
        int directionInt = getPOV();
        
        // Search through the DPadDirection enum to find one which corresponds with the current directionInt
        for (DPadDirection direction : DPadDirection.values()) {
            if (directionInt == direction.direction) return Optional.of(direction);
        }
        
        // If none matched, then return the empty optional
        return Optional.empty();
        
    }
    
    /**
     * Check whether a given {@link DPadDirection} is actively being pressed.
     * @param direction The {@code DPadDirection} to check against.
     * @return          Whether the given direction is held down on the D-pad.
     */
    public boolean isDPadPressed (DPadDirection direction) {
        Optional<DPadDirection> currentDirection = getDPad();
        return currentDirection.isPresent() && currentDirection.get().equals(direction);
    }
    
    /**
     * An enum representing the buttons or orientations of the D-pad.
     * @see RaptorsXboxController#getDPad()
     */
    public enum DPadDirection {
        
        TOP             (0),
        TOP_RIGHT       (45),
        RIGHT           (90),
        BOTTOM_RIGHT    (135),
        BOTTOM          (180),
        BOTTOM_LEFT     (225),
        LEFT            (270),
        TOP_LEFT        (315);
        
        /**
         * Direction in degrees, starting with 0 for {@link DPadDirection#TOP} and progressing clockwise.
         */
        public final int direction;
        
        /**
         * The {@link Rotation2d} of this direction, as if it were placed on the unit circle.
         * This conforms to the standard WPILib understanding of rotation, as zero rotation corresponds
         * with {@link DPadDirection#RIGHT}, and the rotation progresses counterclockwise.
         */
        public final Rotation2d rotation;
        
        private DPadDirection (int direction) {
            this.direction = direction;
            
            // Convert the direction degrees into rotation degrees on the unit circle
            int unitCircleDegrees = 90 - direction;
            if (unitCircleDegrees < 0) unitCircleDegrees += 360;
            rotation = Rotation2d.fromDegrees(unitCircleDegrees);
        }
        
    }
    
}
