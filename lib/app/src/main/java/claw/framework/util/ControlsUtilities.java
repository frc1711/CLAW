
//This is a utility file meant for easily applying complex control management methods
public class ControlsUtilities {
    // Prevent this class from being instantiated.
	private ControlsUtilities() {}
	
	/**
	 * Returns the input value so long as its absolute value is greater than the
	 * specified deadband. If the absolute value of the input value is less than
	 * the specified deadband, 0 will be returned.
	 * 
	 * @param input The input value.
	 * @param deadband The deadband.
	 * @return The input value, or 0 if the absolute value of the input value is
	 * less than the specified deadband.
	 */
	public static double applyDeadband(double input, double deadband) {

		return Math.abs(input) < deadband ? 0 : input;

	}

	public static double applyCircularDeadband (double xInput, double yInput, double deadband, boolean valueToRetrieve) {

		double hypotenuse = Math.sqrt(Math.pow(xInput, 2) + Math.pow(yInput, 2));

		if (Math.abs(hypotenuse) < deadband) {
			if (valueToRetrieve) return xInput;
			else return yInput;
		}
		
		else return 0;
	}
    
	/**
	 * Returns the new value so long as its delta from the old value does not
     * exceed the specified maximum permissible amount. If this delta is greater
	 * than what is permissible, the returned value will instead be the old
	 * value plus the maximum permissible delta in the direction of the new
	 * value.
	 * 
	 * @param oldValue The old value of the variable.
	 * @param newValue The new value of the variable.
	 * @param maxDelta The maximum allowable change.
	 * @return The new value, or the old value plus the maximum permissible
     * delta in the direction of the new value, if the delta between the old and
     * new values exceeds what is permissible.
	 */
	public static double enforceMaximumDelta(
		double oldValue,
		double newValue,
		double maxDelta
	) {
        
		double change = newValue - oldValue;
        
		if (Math.abs(change) <= maxDelta) return newValue;
		else return oldValue + Math.copySign(maxDelta, change);
        
	}
    
    /**
     * Returns the new value so long as it is either moving towards zero, or its
	 * delta from the old value does not exceed the specified maximum
	 * permissible amount. If this delta is greater than what is permissible,
	 * the returned value will instead be the old value plus the maximum
	 * permissible delta in the direction of the new value.
     * 
     * @param oldValue The old value of the variable.
     * @param newValue The new value of the variable.
     * @param maxIncrease The maximum allowable increase.
     * @return The new value, or the old value plus the maximum permissible
	 * delta in the direction of the new value, if the delta between the old and
	 * new values exceeds what is permissible and the new value is not moving
	 * towards zero.
     */
	public static double enforceMaximumPositiveDelta(
		double oldValue,
		double newValue,
		double maxIncrease
	) {
        
		boolean isValueIncreasing = newValue > oldValue;
		boolean isMovementTowardsZero =
			(isValueIncreasing && oldValue < 0) ||
			(!isValueIncreasing && oldValue > 0);
        
		if (isMovementTowardsZero) return newValue;
		
		return isValueIncreasing ?
			Math.min(newValue, oldValue + maxIncrease) :
			Math.max(newValue, oldValue - maxIncrease);
        
	}
}
