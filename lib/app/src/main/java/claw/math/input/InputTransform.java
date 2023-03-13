package claw.math.input;

import claw.math.Transform;

/**
 * A class which helps to process input from a scalar or vector input on a controller (trigger or joystick),
 * handling a deadband zone and applying a curve to refine control.
 */
public class InputTransform implements Transform {
    
    private static final Transform INPUT_CLAMP = Transform.clamp(-1, 1);
    
    /**
     * A {@link Transform} which represents no input curve applied.
     */
    public static final Transform NO_CURVE = Transform.NONE;
    
    /**
     * f(x) = x^(3/2) as an odd function (negated for {@code x < 0}).
     */
    public static final Transform THREE_HALVES_CURVE = Transform.toOdd(x -> Math.pow(x, 1.5));
    
    /**
     * f(x) = x^2 as an odd function (negated for {@code x < 0}).
     */
    public static final Transform SQUARE_CURVE = Transform.toOdd(x -> x*x);
    
    /**
     * Creates a {@link Transform} representing a deadband's application to some input value.
     * @param deadband  A deadband value on the interval [0, 1).
     * @return          The deadband {@code Transform}.
     */
    public static final Transform makeDeadband (double deadband) {
        if (deadband < 0 || deadband >= 1)
            throw new IllegalArgumentException("A deadband must be on the interval [0, 1).");
        
        // Transform.toOdd will allow us to ignore negative input values:
        return Transform.toOdd((input -> {
            // Return 0 if the input is within the deadband
            if (input <= deadband) return 0;
            
            // Apply the deadband
            return input / (1 - deadband);
        }));
    }
    
    private final Transform innerTransform;
    
    /**
     * Create an {@link InputTransform} which can be applied to any input (but designed for handling human input
     * from one (scalar) or two (vector) axes on a controller). The transform applies a deadband
     * so that any input with a magnitude less than the deadband value will be ignored.
     * A given input map is then applied to the output from this deadband, and finally
     * the output is clamped to the range [-1, 1].
     * 
     * @param inputMap      Any {@code Transform}. It is recommended but not required that this transform map -1 to -1,
     * 0 to 0, and 1 to 1. See {@link Transform#toOdd(Transform)} for a easy way to convert an existing transform which
     * works for positive numbers into one which will also properly handle negative numbers. If you're not sure where
     * to start, try out {@link InputTransform#THREE_HALVES_CURVE}.
     * @param deadbandValue Any input with a magnitude of less than this deadband value will be mapped to zero.
     */
    public InputTransform (Transform inputMap, double deadbandValue) {
        innerTransform =
            makeDeadband(deadbandValue)
            .then(inputMap)
            .then(INPUT_CLAMP);
    }
    
    /**
     * Applies this {@link InputTransform} to the given input. The input is assumed to be on the range [-1, 1], but
     * this transform will still clamp input to that range in case it isn't already. The input transform
     * is best used when applying input directly to a value taken from a joystick axis on a controller.
     */
    @Override
    public double apply (double input) {
        return innerTransform.apply(input);
    }
    
}
