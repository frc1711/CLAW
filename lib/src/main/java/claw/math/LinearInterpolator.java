package claw.math;

import java.util.Arrays;

/**
 * A linear interpolator {@link Transform} which can take in x-coordinates and provide a corresponding y-coordinate
 * according to a provided dataset.
 */
public class LinearInterpolator implements Transform {
    
    /**
     * A record representing a point on the x-y plane, which can be used to perform interpolation calculations.
     */
    public static record Point (double x, double y) { }
    
    private final Point[] points;
    
    /**
     * Create a linear interpolator from the given points to interpolate between.
     * @param points A varargs list of points to interpolate beween.
     */
    public LinearInterpolator (Point... points) {
        // Create an array of the points, sorted by x value
        Point[] sortedPoints = points.clone();
        Arrays.sort(sortedPoints, (Point a, Point b) ->
            a.x < b.x ? -1 : (a.x > b.x ? 1 : 0)
        );
        
        this.points = sortedPoints;
    }
    
    /**
     * Create a linear interpolator from the given x and y coordinates representing points to interpolate between.
     * @param xyPairs   Pairs of {@code double} coordinates (x, y), (x, y), (x, y)...
     * Because the given {@code xyPairs} represents pairs of x and y coordinates, an odd number of {@code xyPairs}
     * values is invalid. For example, the input {@code new LinearInterpolator(1, 2, 3, 4, 5, 6)} would represent
     * the points (1, 2), (3, 4) and (5, 6).
     */
    public LinearInterpolator (double... xyPairs) {
        this(xyPairsToPoints(xyPairs));
    }
    
    private static Point[] xyPairsToPoints (double... xyPairs) {
        // Only allow an even number of coordinates (otherwise, we'd be missing a y coordinate for the last point)
        if (xyPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Missing a y coordinate for the last point (number of coordinates given must be even)");
        }
        
        // Create an array of points from the xyPairs list
        Point[] points = new Point[xyPairs.length / 2];
        for (int i = 0; i < points.length; i ++) {
            points[i] = new Point(xyPairs[2*i], xyPairs[2*i + 1]);
        }
        
        return points;
    }
    
    /**
     * Perform a linear interpolation on two points {@code a} and {@code b} in order to estimate a reasonable
     * y-coordinate corresponding with the input x-coordinate {@code x}.
     * @param x The input x-coordinate.
     * @param a The first of two points which form the basis of the linear interpolation.
     * @param b The second of two points which form the basis of the linear interpolation.
     * @return  An output y-coordinate corresponding to the input {@code x}.
     */
    public static double interpolate (double x, Point a, Point b) {
        // If a and b have exactly the same x value, just get the mean of their y values
        if (a.x == b.x) return (a.y + b.y) / 2;
        
        // Calculate p. If p=0, (x,y) should be on point a, if p=1, (x,y) should be on point b
        double p = (x - a.x) / (b.x - a.x);
        
        // Given p, perform the linear interpolation between points a and b
        return p * (b.y - a.y) + a.y;
    }
    
    /**
     * Get a corresponding y coordinate for the given x coordinate by performing a linear interpolation betwen
     * the points provided to this {@link LinearInterpolator}.
     * @param x The x input coordinate.
     * @return  The corresponding y output coordinate.
     */
    @Override
    public double apply (double x) {
        
        // Return a constant value if there are exactly 0 or 1 given points, as all later calculations
        // depends on there being at least two points
        if (points.length == 0) return 0.;
        if (points.length == 1) return points[0].y;
        final int lastIndex = points.length - 1;
        
        // If x is further to the left than the first point, we interpolate between the first two points
        if (x < points[0].x) return interpolate(x, points[0], points[1]);
        
        // Find the first point which is further to the right than the given x
        for (int i = 0; i <= lastIndex; i ++) {
            
            // Return the y coordinate of this point if it exactly matches the given x coordinate
            if (points[i].x == x) return points[i].y;
            
            // Because points[i] is the first point further to the right than x, points[i-1] must
            // also be the last point further to the left than x, so we interpolate between the two
            if (points[i].x > x) {
                return interpolate(x, points[i-1], points[i]);
            }
            
        }
        
        // Because no point was further to the right than x, x is beyond the last point and we must
        // interpolate between the last two points
        return interpolate(x, points[lastIndex - 1], points[lastIndex]);
        
    }
    
}
