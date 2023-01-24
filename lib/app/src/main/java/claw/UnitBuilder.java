package claw;

import java.util.HashMap;
import java.util.Set;

public class UnitBuilder {
    
    private static final HashMap<String, LiveUnit> units = new HashMap<>();
    
    static Set<String> getUnitNames () {
        return units.keySet();
    }
    
    public static LiveUnit getUnitByName (String name) {
        return units.get(name);
    }
    
    private boolean isFinalized = false;
    
    public LiveUnit withName (String name) {
        throwIfFinalized();
        
        if (units.containsKey(name))
            throw new RuntimeException("The LiveUnit '"+name+"' cannot be created more than once.");
        
        isFinalized = true;
        LiveUnit unit = new LiveUnit(name);
        units.put(name, unit);
        return unit;
    }
    
    private void throwIfFinalized () {
        if (isFinalized) throw new RuntimeException("Cannot use a UnitBuilder after it has already made a LiveUnit");
    }
    
}
