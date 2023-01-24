package claw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry class which stores named items and throws a {@link NamedConflictException} when attempting to
 * add an item whose name is already in the registry.
 */
// TODO: Remove the Registry class
public class Registry <T> {
    
    private final Map<String, T> items = new HashMap<String, T>();
    
    private final String itemsType;
    
    /**
     * Creates a new registry which throws a {@link NameConflictException} when {@link #add(String, Object)}
     * is called on an item whose name is already in the registry.
     * @param itemsType A string to use to describe the type of item the registry stores (e.g. "device",
     * "config field").
     */
    public Registry (String itemsType) {
        this.itemsType = itemsType;
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
     * @throws NameConflictException    If an item in the registry already has the provided name.
     */
    public void add (String name, T item) throws NameConflictException {
        // Throw an exception if the item already exists in the registry
        if (hasItem(name))
            throw new NameConflictException(itemsType, name);
        
        // Add the item to the registry
        items.put(name, item);
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
     * Get all items in the registry. This collection is backed by an internal
     * {@code Map} and therefore must not be mutated.
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
     * An exception which can be thrown by a {@link Registry} if trying to add an item to the registry whose name already
     * exists in the registry.
     */
    public static class NameConflictException extends Exception {
        public NameConflictException (String itemsType, String name) {
            super(itemsType+" \""+name+"\" already exists in the "+itemsType+" registry.");
        }
    }
    
}
