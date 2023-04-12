package claw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import claw.actions.compositions.Context.TerminatedContextException;
import claw.rct.base.console.ConsoleManager;

/**
 * A class allowing for the live updating of fields to be displayed in the console.
 */
public class LiveValues {
    
    /**
     * An object lock to be synchronized on before making any modifications to the fields,
     * values, etc.
     */
    private final Object fieldsLock = new Object();
    
    private final ArrayList<String> fields = new ArrayList<>();
    private final ArrayList<String> values = new ArrayList<>();
    private final HashSet<String> newFieldNames = new HashSet<>();
    private final HashSet<String> updatedFields = new HashSet<>();
    
    /**
     * Set a live field to a string value to be displayed in the console.
     * @param fieldName The name of the field under which the value should be displayed.
     * @param value     The value to put to the console.
     */
    public void setField (String fieldName, String value) {
        synchronized (fieldsLock) {
            // Get the index of the field in the fields and values arrays
            int fieldIndex = fields.indexOf(fieldName);
            
            if (fieldIndex == -1) {
                
                // Add a new field and value
                newFieldNames.add(fieldName);
                fields.add(fieldName);
                values.add(value);
                
            } else if (!values.get(fieldIndex).equals(value)) {
                
                // Update the value only, but first check if it's the same as it was before
                values.set(fieldIndex, value);
                updatedFields.add(fieldName);
                
            }
        }
    }
    
    /**
     * Set a live field to a double value to be displayed in the console.
     * @param fieldName The name of the field under which the value should be displayed.
     * @param value     The value to put to the console.
     */
    public void setField (String fieldName, double value) {
        setField(fieldName, Double.toString(value));
    }
    
    /**
     * Set a live field to a boolean value to be displayed in the console.
     * @param fieldName The name of the field under which the value should be displayed.
     * @param value     The value to put to the console.
     */
    public void setField (String fieldName, boolean value) {
        setField(fieldName, Boolean.toString(value));
    }
    
    /**
     * Set a live field to an integer value to be displayed in the console.
     * @param fieldName The name of the field under which the value should be displayed.
     * @param value     The value to put to the console.
     */
    public void setField (String fieldName, int value) {
        setField(fieldName, Integer.toString(value));
    }
    
    /**
     * Update all the fields in the console to display the latest values.
     * @param console
     */
    public void update (ConsoleManager console) throws TerminatedContextException {
        Set<String> updatedFieldsCopy, newFieldNamesCopy;
        List<String> fieldsCopy, valuesCopy;
        
        synchronized (fieldsLock) {
            // Do nothing if no fields have been changed
            if (updatedFields.size() == 0 && newFieldNames.size() == 0) {
                return;
            }
            
            // Copy data synchronously
            updatedFieldsCopy = Set.copyOf(updatedFields);
            newFieldNamesCopy = Set.copyOf(newFieldNames);
            fieldsCopy = List.copyOf(fields);
            valuesCopy = List.copyOf(values);
            
            // Clear updated and new fields
            newFieldNames.clear();
            updatedFields.clear();
        }
        
        // Move up to the top of the preexisting lines (lines for fields already printed to the console)
        int preexistingLines = fieldsCopy.size() - newFieldNamesCopy.size();
        console.moveUp(preexistingLines);
        
        // Iterate through all existing fields
        for (int i = 0; i < fieldsCopy.size(); i ++) {
            String fieldName = fieldsCopy.get(i);
            String value = valuesCopy.get(i);
            
            // Check for no-change vs. updated vs. new field
            if (newFieldNamesCopy.contains(fieldName)) {
                
                // Entirely new field, print a new line
                printField(console, fieldName, value);
                
            } else if (updatedFieldsCopy.contains(fieldName)) {
                
                // Updated field, clear the line then replace it
                console.clearLine();
                printField(console, fieldName, value);
                
            } else {
                
                // No change to field, move on to next field
                console.moveUp(-1);
                
            }
            
        }
        
        console.flush();
        
    }
    
    /**
     * Print a single field to the console
     */
    private static void printField (ConsoleManager console, String fieldName, String value) throws TerminatedContextException {
        String space = " ".repeat(Math.max(0, 18 - fieldName.length()));
        console.println(fieldName + " : " + space + value);
    }
    
}