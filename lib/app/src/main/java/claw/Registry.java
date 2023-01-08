package claw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import claw.logs.RCTLog;

public class Registry <T> {
    
    private final Map<String, T> items = new HashMap<String, T>();
    
    private final String itemsType;
    private final RCTLog log;
    
    public Registry (String itemsType, RCTLog log) {
        this.itemsType = itemsType;
        this.log = log;
    }
    
    public boolean hasItem (String name) {
        return items.containsKey(name);
    }
    
    public T getItem (String name) {
        return items.get(name);
    }
    
    public void add (String name, T item) {
        // Log a warning if the item already exists in the registry
        if (hasItem(name))
            log.out("Warning: "+itemsType+" \""+name+"\" already exists in the "+itemsType+" registry");
        
        // Add the item to the registry
        items.put(name, item);
    }
    
    public List<String> getItemNames () {
        List<String> itemNames = new ArrayList<>(items.keySet());
        itemNames.sort((a, b) -> a.compareTo(b));
        return itemNames;
    }
    
}
