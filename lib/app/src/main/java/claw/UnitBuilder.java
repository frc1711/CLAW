package claw;

import java.util.List;

import claw.internal.Registry;
import claw.internal.Registry.NameConflictException;

public class UnitBuilder {
    
    private static final Registry<LiveUnit> units = new Registry<>("unit");
    
    public static List<String> getUnitNames () {
        return units.getItemNames();
    }
    
    public static LiveUnit getUnitByName (String name) {
        return units.getItem(name);
    }
    
    private boolean isFinalized = false;
    
    public LiveUnit withName (String name) {
        throwIfFinalized();
        
        LiveUnit unit = new LiveUnit(name);
        try {
            units.add(name, unit);
        } catch (NameConflictException e) {
            throw new RuntimeException(e);
        }
        
        isFinalized = true;
        return unit;
    }
    
    private void throwIfFinalized () {
        if (isFinalized) throw new RuntimeException("Cannot use a UnitBuilder after it has already made a LiveUnit");
    }
    
}
