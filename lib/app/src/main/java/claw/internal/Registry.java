package claw.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import claw.api.logs.CLAWLogger;

/**
 * A registry class which stores named items and can be configured in name conflict handling.
 */
public class Registry <T> {
    
    private final Map<String, T> items = new HashMap<String, T>();
    
    private final String itemsType;
    private final Optional<CLAWLogger> log;
    
    /**
     * Creates a new registry which logs a warning to the given {@link CLAWLogger} when {@link #add(String, Object)}
     * is called on an item whose name is already in the registry.
     * @param itemsType A string to use to describe the type of item the registry stores (e.g. "device",
     * "config field").
     * @param log       The {@code CLAWLogger} to use for logging name conflict warnings.
     */
    public Registry (String itemsType, CLAWLogger log) {
        this.itemsType = itemsType;
        this.log = Optional.ofNullable(log);
    }
    
    /**
     * Creates a new registry which throws a {@link NameConflictException} when {@link #add(String, Object)}
     * is called on an item whose name is already in the registry.
     * @param itemsType A string to use to describe the type of item the registry stores (e.g. "device",
     * "config field").
     */
    public Registry (String itemsType) {
        this(itemsType, null);
    }
    
    /**
     * Checks whether the registry contains an item with a given name.
     * @param name  The item name to check against.
     * @return      Whether the item name exists in the registry.
     */
    public boolean hasItem (String name) {
        return items.containsKey(name);
    }
    
    /**
     * Gets a named item from the registry.
     * @param name  The item's name.
     * @return      The item if it exists in the registry, {@code null} otherwise.
     */
    public T getItem (String name) {
        return items.get(name);
    }
    
    /**
     * Add a named item to the registry.
     * @param name                      The item's name.
     * @param item                      The item.
     * @throws NameConflictException    If an item in the registry already has the provided name,
     * there is a name conflict. If the registry is configured to throw an exception when
     * a name conflict arises, this runtime exception will be thrown.
     * Otherwise, name conflicts will be logged as warnings to an {@link CLAWLogger} provided to the registry.
     * In either case, if a name conflict occurs, the addition of the item to the registry will fail.
     */
    public void add (String name, T item) throws NameConflictException {
        try {
            
            // Throw an exception if the item already exists in the registry
            if (hasItem(name))
                throw new NameConflictException(itemsType, name);
            
            // Add the item to the registry
            items.put(name, item);
            
        } catch (NameConflictException e) {
            
            // If the log optional is not empty, then we should log the exception instead of throwing it
            if (log.isPresent()) {
                // Log the exception
                log.get().out("Warning: " + e.getMessage());
            } else {
                // Otherwise, we should throw the name conflict exception
                throw e;
            }
            
        }
    }
    
    /**
     * Get all item names in the registry, sorted alphabetically.
     * @return A {@code List<String>} containing the item names.
     */
    public List<String> getItemNames () {
        List<String> itemNames = new ArrayList<>(items.keySet());
        itemNames.sort((a, b) -> a.compareTo(b));
        return itemNames;
    }
    
    /**
     * Get all items in the registry.
     * @return A {@code Collection<T>} containing the items.
     */
    public Collection<T> getAllItems () {
        return items.values();
    }
    
    /**
     * Get the size of the registry.
     * @return The number of items in the registry.
     */
    public int getSize () {
        return items.size();
    }
    
    /**
     * A runtime exception which can be thrown by a {@link Registry} if an item
     */
    public static class NameConflictException extends RuntimeException {
        public NameConflictException (String itemsType, String name) {
            super(itemsType+" \""+name+"\" already exists in the "+itemsType+" registry");
        }
    }
    
}
