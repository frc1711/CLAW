package claw.math;

import java.util.Optional;

import edu.wpi.first.math.Nat;
import edu.wpi.first.math.Num;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.interpolation.Interpolatable;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.numbers.N4;

/**
 * A vector class which can be used for calculations.
 */
public class Vector <N extends Num> implements Interpolatable<Vector<N>> {
    
    private Optional<Double> magnitude = Optional.empty();
    private Optional<Rotation2d> angle = Optional.empty();
    private final Nat<N> dimensionality;
    private final double[] components;
    
    /**
     * Gets a {@link Vector} from a list of components and the dimensionality of the vector.
     * @param dim           The dimensionality of the vector as an instance of {@link Nat}. If this dimensionality
     * does not match the length of the {@code components} array, an exception will be thrown.
     * @param components    An array containing the vector's components.
     */
    public Vector (Nat<N> dim, double... components) {
        if (dim.getNum() != components.length)
            throw new IllegalArgumentException("The number of components given for this Vector does not match the dimensionality of the Vector");
        dimensionality = dim;
        this.components = components.clone();
    }
    
    /**
     * Create a new two-dimensional vector from the given angle and magnitude.
     * @param angle         The angle of the vector, as a {@link Rotation2d}.
     * @param magnitude     The magnitude of the vector.
     * @return              The vector with the given {@code angle} and {@code magnitude}.
     */
    public static Vector<N2> from (Rotation2d angle, double magnitude) {
        return Vector.from(
            magnitude * angle.getCos(),
            magnitude * angle.getSin()
        );
    }
    
    /**
     * Create a new one-dimensional vector {@code <x>} from the given x component.
     * @param x The x component of the vector.
     * @return  The vector {@code <x>}.
     */
    public static Vector<N1> from (double x) {
        return new Vector<>(Nat.N1(), x);
    }
    
    /**
     * Create a new two-dimensional vector {@code <x, y>} from the given components.
     * @param x The x component of the vector.
     * @param y The y component of the vector.
     * @return  The vector {@code <x, y>}.
     */
    public static Vector<N2> from (double x, double y) {
        return new Vector<>(Nat.N2(), x, y);
    }
    
    /**
     * Create a new three-dimensional vector {@code <x, y, z>} from the given components.
     * @param x The x component of the vector.
     * @param y The y component of the vector.
     * @param z The z component of the vector.
     * @return  The vector {@code <x, y, z>}.
     */
    public static Vector<N3> from (double x, double y, double z) {
        return new Vector<>(Nat.N3(), x, y, z);
    }
    
    /**
     * Create a new four-dimensional vector {@code <x, y, z, w>} from the given components.
     * @param x The x component of the vector.
     * @param y The y component of the vector.
     * @param z The z component of the vector.
     * @param w The w component of the vector.
     * @return  The vector {@code <x, y, z, w>}.
     */
    public static Vector<N4> from (double x, double y, double z, double w) {
        return new Vector<>(Nat.N4(), x, y, z, w);
    }
    
    /**
     * Get the dimensionality of this vector (i.e. the number of components it has), as an integer.
     * @return The dimensionality of this vector.
     */
    public int getDimensionality () {
        return dimensionality.getNum();
    }
    
    /**
     * Get the {@code i+1}th component of this vector. For example,
     * {@code getComponent(2)} would return the third component.
     * @param i The index of the component to retrieve.
     * @return  The {@code i+1}th component of this vector.
     * @throws  IllegalArgumentException If the given component index {@code i} is invalid for this vector
     * (i.e. no {@code i+1}th component exists for this vector).
     */
    public double getComponent (int i) throws IllegalArgumentException {
        if (i < 0) {
            throw new IllegalArgumentException("Cannot retrieve a component with index less than zero");
        } else if (i >= getDimensionality()) {
            throw new IllegalArgumentException(
                "Cannot retrieve component with index "+i+" from a " +
                getDimensionality()+"-dimensional vector"
            );
        } else {
            return components[i];
        }
    }
    
    /**
     * Get the x (1st) component of this vector.
     * If this vector's dimensionality is less than one, an exception will be thrown.
     * @return The x component of this vector.
     */
    public double getX () {
        return getComponent(0);
    }
    
    /**
     * Get the y (2nd) component of this vector.
     * If this vector's dimensionality is less than two, an exception will be thrown.
     * @return The y component of this vector.
     */
    public double getY () {
        return getComponent(1);
    }
    
    /**
     * Get the z (3rd) component of this vector.
     * If this vector's dimensionality is less than three, an exception will be thrown.
     * @return The z component of this vector.
     */
    public double getZ () {
        return getComponent(2);
    }
    
    /**
     * Get the w (4th) component of this vector.
     * If this vector's dimensionality is less than four, an exception will be thrown.
     * @return The w component of this vector.
     */
    public double getW () {
        return getComponent(3);
    }
    
    /**
     * Gets the magnitude of the vector.
     * @return The magnitude.
     */
    public double getMagnitude () {
        if (magnitude.isEmpty()) {
            double squaredComponents = 0;
            for (double component : components)
                squaredComponents += component * component;
            
            magnitude = Optional.of(Math.sqrt(squaredComponents));
        }
        
        return magnitude.get();
    }
    
    /**
     * Applies a {@link Transform} to every component of this vector, returning a vector
     * with the same dimensionality but with all components being the application of the
     * transform on this vector's corresponding component.
     * @param transform The transform to apply to each component.
     * @return          The result of the transformation applied to each component of this vector.
     */
    public Vector<N> apply (Transform transform) {
        double[] newComponents = new double[components.length];
        for (int i = 0; i < components.length; i ++)
            newComponents[i] = transform.apply(components[i]);
        return new Vector<N>(dimensionality, newComponents);
    }
    
    /**
     * Returns this vector scaled by a given coefficient.
     * @param k The coefficient to scale this vector by.
     * @return  The scaled vector result.
     */
    public Vector<N> scale (double k) {
        return this.apply(a -> a*k);
    }
    
    /**
     * Returns this vector scaled so that the new magnitude is equal to the given magnitude. If this vector's magnitude
     * is zero, a zero vector will be returned.
     * @param magnitude The magnitude of the vector after scaling.
     * @return          This vector, scale such that the magnitude equals the provided magnitude.
     */
    public Vector<N> scaleToMagnitude (double magnitude) {
        if (getMagnitude() == 0)
            return this.scale(0);
        else
            return this.scale(magnitude / getMagnitude());
    }
    
    /**
     * Negates this vector, resulting in a vector of equal magnitude and opposite direction.
     * @return This vector, but negated.
     */
    public Vector<N> negate () {
        return this.apply(a -> -a);
    }
    
    /**
     * Apply an operation to every component of the vector
     */
    private Vector<N> apply (Vector<N> other, Operation operation) {
        double[] newComponents = new double[components.length];
        for (int i = 0; i < components.length; i ++)
            newComponents[i] = operation.apply(components[i], other.components[i]);
        return new Vector<N>(dimensionality, newComponents);
    }
    
    private static interface Operation {
        public double apply (double a, double b);
    }
    
    /**
     * Calculate the dot product of this vector and other.
     * @param other The vector to calculate the dot product with.
     * @return      The dot product.
     */
    public double dotProduct (Vector<N> other) {
        // Compute a vector containing the products of this vector and the other vector's components
        Vector<N> productVector = this.apply(other, (a, b) -> a * b);
        
        // Sum the product components
        double sum = 0;
        for (double component : productVector.components)
            sum += component;
        
        // Return the dot product
        return sum;
    }
    
    /**
     * Add this vector and another together.
     * @param other The vector to add.
     * @return      The sum of the two vectors.
     */
    public Vector<N> add (Vector<N> other) {
        return this.apply(other, (a, b) -> a + b);
    }
    
    /**
     * Subtract another vector from this one.
     * @param other The vector to subtract.
     * @return      The difference of the two vectors.
     */
    public Vector<N> subtract (Vector<N> other) {
        return this.apply(other, (a, b) -> a - b);
    }
    
    /**
     * Returns the angle formed between a two-dimensional vector and the x-axis, in radians. This angle
     * increases counterclockwise. For example, a vector facing in the +y direction will return a {@link Rotation2d}
     * equivalent to pi/2 radians.
     * @param vector    The two-dimensional vector to retrieve the direction angle of.
     * @return          The angle of the vector as a {@code Rotation2d}.
     */
    public static Rotation2d getAngle (Vector<N2> vector) {
        if (vector.angle.isEmpty()) {
            vector.angle = Optional.of(new Rotation2d(vector.getX(), vector.getY()));
        }
        
        return vector.angle.get();
    }
    
    /**
     * Get the output of rotating a two-dimensional vector by a set amount.
     * @param vector    The two-dimensional vector to rotate.
     * @param rotation  The rotation which the vector should be rotated by.
     * @return          The rotated vector.
     */
    public static Vector<N2> rotateBy (Vector<N2> vector, Rotation2d rotation) {        
        double x = vector.getX();
        double y = vector.getY();
        double sin = rotation.getSin();
        double cos = rotation.getCos();
        
        return Vector.from(
            cos * x - sin * y,
            sin * x + cos * y
        );
    }
    
    @Override
    public Vector<N> interpolate (Vector<N> endValue, double t) {
        return endValue.scale(t).add(this.scale(1-t));
    }
    
    @Override
    public String toString () {
        String[] componentsStrings = new String[components.length];
        for (int i = 0; i < components.length; i ++)
            componentsStrings[i] = Double.toString(components[i]);
        return "<" + String.join(", ", componentsStrings) + ">";
    }
    
}
