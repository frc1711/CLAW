package claw.api.devices;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ConfigBuilder {
    
    private final Map<String, ConfigMethod<String>> methods = new HashMap<String, ConfigMethod<String>>();
    private final Map<String, Supplier<String>> fields = new HashMap<String, Supplier<String>>();
    
    public void callMethod (String line) throws BadMethodCall {
        line = line.trim();
        
        // Check whether or not the command call is empty
        if (line.isEmpty())
            throw new BadMethodCall("Method call is empty");
        
        // Check if the map of config methods includes the method from the command
        String methodName = line.split(" ")[0];
        if (!methods.containsKey(methodName))
            throw new BadMethodCall("Method does not exist");
        
        // Call the method config
        String allArgsString = line.substring(methodName.length());
        methods.get(methodName).call(allArgsString.trim());
    }
    
    /**
     * Returns {@code null} if the field does not exist
     * @param line
     * @return
     */
    public String readField (String name) {
        name = name.trim();
        if (fields.containsKey(name))
            return fields.get(name).get();
        else return null;
    }
    
    public Set<String> getFields () {
        return fields.keySet();
    }
    
    public Set<String> getMethods () {
        return methods.keySet();
    }
    
    public void addStringMethod (String name, ConfigMethod<String> method) {
        methods.put(name, method);
    }
    
    public void addIntMethod (String name, ConfigMethod<Integer> method) {
        addStringMethod(name, (String args) -> {
            try {
                int intArg = Integer.parseInt(args);
                method.call(intArg);
            } catch (NumberFormatException e) {
                throw new BadMethodCall("The argument could not be parsed to an integer");
            }
        });
    }
    
    public void addDoubleMethod (String name, ConfigMethod<Double> method) {
        addStringMethod(name, (String args) -> {
            try {
                double doubleArg = Double.parseDouble(args);
                method.call(doubleArg);
            } catch (NumberFormatException e) {
                throw new BadMethodCall("The argument could not be parsed to a double");
            }
        });
    }
    
    public void addMethod (String name, Runnable method) {
        addStringMethod(name, s -> method.run());
    }
    
    public void addField (String name, Supplier<String> getter) {
        fields.put(name, getter);
    }
    
    public interface ConfigMethod<T> {
        public void call (T arg) throws BadMethodCall;
    }
    
    public static class BadMethodCall extends Exception {
        public BadMethodCall (String message) {
            super(message);
        }
    }
    
}
