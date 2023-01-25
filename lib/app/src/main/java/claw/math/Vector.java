package claw.math;

import java.util.Optional;

import edu.wpi.first.math.Nat;
import edu.wpi.first.math.Num;

/**
 * A type-safe vector class which can be used for calculations.
 */
public class Vector <N extends Num> {
    
    private Optional<Double> magnitude = Optional.empty();
    private final Nat<N> dimensionality;
    
    /**
     * An array of components backed by the {@link Vector} (meaning these components should never be modified).
     * For example, for a two-dimensional vector {@code Vector<N2>} or {@link Vector2D}, this array would
     * have a length of two and would contain the x and y components of the vector as {@code [x, y]}.
     */
    public final double[] components;
    
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
     * Applies a {@link Transform} to the magnitude of this vector. 
     * @param transform The {@code Transform} to apply to this vector's magnitude.
     * @return          This vector scaled such that the magnitude of the resulting vector is equal to the result
     * of the transform applied to this vector's magnitude.
     */
    public Vector<N> applyScale (Transform transform) {
        if (getMagnitude() == 0)
            return this.scale(0);
        
        double newMagnitude = transform.apply(getMagnitude());
        return this.scale(newMagnitude / getMagnitude());
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
        return this.apply(other, (a, b) -> a * b);
    }
    
    /**
     * Subtract another vector from this one.
     * @param other The vector to subtract.
     * @return      The difference of the two vectors.
     */
    public Vector<N> subtract (Vector<N> other) {
        return this.apply(other, (a, b) -> a - b);
    }
    
}
