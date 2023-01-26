package claw.testing;

import java.util.ArrayList;
import java.util.List;

import claw.rct.network.low.ConsoleManager;

public abstract class SystemsCheck {
    
    public static final ArrayList<SystemsCheck> systemsChecks = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    public static List<SystemsCheck> getAllSystemsChecks () {
        return (ArrayList<SystemsCheck>)systemsChecks.clone();
    }
    
    public static void addSystemsCheck (SystemsCheck check) {
        systemsChecks.add(check);
    }
    
    private final String systemName;
    
    public SystemsCheck (String systemName) {
        this.systemName = systemName;
    }
    
    public String getSystemName () {
        return systemName;
    }
    
    public abstract void run (ConsoleManager console);
    public abstract boolean isActuatingCheck ();
    
}
