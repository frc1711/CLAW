package claw.math.input;

import claw.math.Transform;
import claw.math.Vector;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.wpilibj.XboxController;

public class RaptorsXboxController {
    
    private final XboxController controller;
    
    private final Transform leftStickTransform, rightStickTransform;
    
    public RaptorsXboxController (XboxController controller) {
        this(controller, Transform.clamp(-1, 1));
    }
    
    public RaptorsXboxController (XboxController controller, Transform stickVectorTransform) {
        this(controller, stickVectorTransform, stickVectorTransform);
    }
    
    public RaptorsXboxController (XboxController controller, Transform leftStickVectorTransform, Transform rightStickVectorTransform) {
        this.controller = controller;
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
    
    public Vector<N2> getLeftStick () {
        return getStickVector(controller.getLeftX(), controller.getLeftY(), leftStickTransform);
    }
    
    public Vector<N2> getRightStick () {
        return getStickVector(controller.getRightX(), controller.getRightY(), rightStickTransform);
    }
    
    
    
}
