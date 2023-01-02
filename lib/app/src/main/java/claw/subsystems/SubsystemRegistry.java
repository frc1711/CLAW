package claw.subsystems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubsystemRegistry {
    
    private final Map<String, SubsystemCLAW> registry = new HashMap<String, SubsystemCLAW>();
    
    public void addSubsystem (SubsystemCLAW subsystem) {
        registry.put(subsystem.getName(), subsystem);
    }
    
    public List<String> getSubsystemNames () {
        List<String> names = new ArrayList<>(registry.keySet());
        names.sort((a, b) -> a.compareTo(b));
        return names;
    }
    
}
