package claw.math;

public class EnumDebouncer <E extends Enum<E>> {
    
    private E baselineValue;
    private E lastInputValue;
    
    private final DualDebouncer inputStabilizedDebouncer;
    
    public EnumDebouncer (E initialValue, double settleTime) {
        inputStabilizedDebouncer = new DualDebouncer(true, 0, settleTime);
        baselineValue = initialValue;
        lastInputValue = initialValue;
    }
    
    public E calculate (E inputValue) {
        
        // This stores whether or not the input has stabilized at a new value (and therefore whether we can change the baseline)
        boolean changeToNewInput = inputStabilizedDebouncer.calculate(inputValue.equals(lastInputValue));
        
        lastInputValue = inputValue;
        
        // Reset the baseline if the input has stabilized at a new value
        if (changeToNewInput && !baselineValue.equals(inputValue)) {
            baselineValue = inputValue;
        }
        
        // Return the (possibly new) baseline
        return baselineValue;
        
    }
    
}
