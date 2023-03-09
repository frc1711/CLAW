package claw;

import java.util.ArrayList;
import java.util.HashSet;

import claw.rct.network.low.ConsoleManager;

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
    public void update (ConsoleManager console) {
        synchronized (fieldsLock) {
            
            // Move up to the top of the preexisting lines
            int preexistingLines = fields.size() - newFieldNames.size();
            console.moveUp(preexistingLines);
            
            // Iterate through all existing fields
            for (int i = 0; i < fields.size(); i ++) {
                String fieldName = fields.get(i);
                String value = values.get(i);
                
                // Check for no-change vs. updated vs. new field
                if (newFieldNames.contains(fieldName)) {
                    
                    // New field
                    printField(console, fieldName, value);
                    
                } else if (updatedFields.contains(fieldName)) {
                    
                    // Updated field
                    console.clearLine();
                    printField(console, fieldName, value);
                    
                } else {
                    
                    // No change to field
                    console.moveUp(-1);
                    
                }
                
            }
            
            // Clear updated fields and new fields
            newFieldNames.clear();
            updatedFields.clear();
            
        }
    }
    
    /**
     * Print a single field to the console
     */
    private static void printField (ConsoleManager console, String fieldName, String value) {
        String space = " ".repeat(Math.max(0, 18 - fieldName.length()));
        console.println(fieldName + " : " + space + value);
    }
    
}