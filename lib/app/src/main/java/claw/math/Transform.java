package claw.math;

/**
 * Represents a functional mapping of objects onto objects of the same type.
 */
@FunctionalInterface
public interface Transform <T> {
    /**
     * f(x) = x
     */
    public static final Transform<Double> NONE = x -> x;
    
    /**
     * f(x) = -x
     */
    public static final Transform<Double> NEGATE = x -> -x;
        
    /**
     * The mathematical sign (sgn) function.
     */
    public static final Transform<Double> SIGN = x -> x == 0 ? 0. : (x > 0 ? 1 : -1);
    
    /**
     * Gets a {@link Transform} which clamps a value to a closed interval.
     * @param low   The minimum output value.
     * @param high  The maximum output value.
     * @return      The described clamp transform.
     */
    public static Transform<Double> clamp (double low, double high) {
        return input -> Math.min(Math.max(input, low), high);
    }
    
    /**
     * Gets a linear {@link Transform} which applies the equation {@code y = mx + b}
     * to the parameter for the given {@code m} and {@code b}.
     * @param m The slope of the line represented by this linear transform.
     * @param b The y-intercept of the line represented by this linear transform.
     * @return  The linear transform.
     */
    public static Transform<Double> linear (double m, double b) {
        return x -> m*x + b;
    }
    
    /**
     * Turns a transform into an even function by reflecting all x values less than zero across the y-axis.
     * @param transform The base {@link Transform} to turn into an even function.
     * @return          The symmetrical (even function) version of the given transform. 
     */
    public static Transform<Double> toEven (Transform<Double> transform) {
        return x -> transform.apply(x >= 0 ? x : -x);
    }
    
    /**
     * Turns a transform into an odd function by reflecting all x values less than zero across the y-axis
     * and negating the output for those reflected values. Note that {@code Transform.toOdd(f).apply(0)} will always
     * yield 0 for any transform function {@code f}.
     * @param transform The base {@link Transform} to turn into an odd function.
     * @return          The symmetrical (odd function) version of the given transform. 
     */
    public static Transform<Double> toOdd (Transform<Double> transform) {
        return x -> toEven(transform).apply(x) * SIGN.apply(x);
    }
    
    /**
     * Composes this {@link Transform} with another. That is,
     * {@code g.then(f)} is equivalent to the mathematical notation
     * "f(g(x))".
     * @param transform The transform to be applied after this one finishes.
     * @return          The composition of the two transforms.
     */
    public default Transform<T> then (Transform<T> transform) {
        return x -> transform.apply(this.apply(x));
    }
    
    /**
     * Apply the functional mapping described by this transformation.
     * @param x The input to the function.
     * @return  The output from the function.
     */
    public T apply (T x);
    
}
