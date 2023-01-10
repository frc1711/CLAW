package claw.internal.rct.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import claw.api.subsystems.SubsystemCLAW;
import claw.internal.rct.network.low.ConsoleManager;
import edu.wpi.first.util.function.BooleanConsumer;
import edu.wpi.first.util.sendable.SendableBuilder;

public class RCTSendableBuilder implements SendableBuilder {
    
    private String dataTypeString = "";
    private boolean isActuator = false;
    private Runnable setSafeStateFunc = () -> { };
    private final Map<String, RCTField> map = new HashMap<String, RCTField>();
    
    public RCTSendableBuilder (ConsoleManager console, SubsystemCLAW subsystem) {
        dataTypeString = subsystem.getName();
    }
    
    public String[] getFieldsDisplay () {
        List<String> lines = new ArrayList<String>();
        
        lines.add(dataTypeString + (isActuator ? " (actuator)" : ""));
        map.keySet().forEach(key -> {
            String line = key + " : " + map.get(key).getter.get();
            lines.add(line);
        });
        
        return lines.toArray(new String[0]);
    }
    
    public void setFieldValue (String fieldName, String newValue) throws NoSuchElementException, BadRepresentationException {
        // If the map does not contain the given field or the setter is null for the particular field,
        // there is no setter to use
        if (map.get(fieldName) == null || map.get(fieldName).setter == null)
            throw new NoSuchElementException();
        
        map.get(fieldName).setter.setField(newValue);
    }
    
    public void enterSafeState () {
        if (isActuator)
            setSafeStateFunc.run();
    }
    
    @Override
    public void setSmartDashboardType (String type) {
        dataTypeString = type;
    }
    
    @Override
    public void setActuator (boolean value) {
        isActuator = value;
    }
    
    @Override
    public void setSafeState (Runnable func) {
        setSafeStateFunc = func;
    }
    
    private void addField (String key, RCTField field) {
        map.put(key, field);
    }
    
    @Override
    public void addBooleanProperty (String key, BooleanSupplier getter, BooleanConsumer setter) {
        addField(key, new RCTField(() -> String.valueOf(getter.getAsBoolean()), str -> {
            str = str.toLowerCase();
            if (str.equals("true") || str.equals("t") || str.equals("1"))
                setter.accept(true);
            else if (str.equals("false") || str.equals("f") || str.equals("0"))
                setter.accept(false);
            else
                throw new BadRepresentationException("Did not receive a valid boolean representation.");
        }));
    }
    
    @Override
    public void addDoubleProperty (String key, DoubleSupplier getter, DoubleConsumer setter) {
        addField(key, new RCTField(() -> String.valueOf(getter.getAsDouble()), str -> {
            try {
                setter.accept(Double.parseDouble(str));
            } catch (NumberFormatException e) {
                throw new BadRepresentationException("Did not receive a valid double representation.");
            }
        }));
    }
    
    @Override
    public void addStringProperty (String key, Supplier<String> getter, Consumer<String> setter) {
        addField(key, new RCTField(getter, str -> setter.accept(str)));
    }
    
    @Override
    public void addBooleanArrayProperty (String key, Supplier<boolean[]> getter, Consumer<boolean[]> setter) {
        addField(key, new RCTField(() -> "boolean["+getter.get().length+"]", str -> {
            throw new BadRepresentationException("Setting arrays is not supported");
        }));
    }
    
    @Override
    public void addDoubleArrayProperty (String key, Supplier<double[]> getter, Consumer<double[]> setter) {
        addField(key, new RCTField(() -> "double["+getter.get().length+"]", str -> {
            throw new BadRepresentationException("Setting arrays is not supported");
        }));
    }
    
    @Override
    public void addStringArrayProperty (String key, Supplier<String[]> getter, Consumer<String[]> setter) {
        addField(key, new RCTField(() -> "String["+getter.get().length+"]", str -> {
            throw new BadRepresentationException("Setting arrays is not supported");
        }));
    }
    
    @Override
    public void addRawProperty (String key, Supplier<byte[]> getter, Consumer<byte[]> setter) {
        addField(key, new RCTField(() -> "byte["+getter.get().length+"]", str -> {
            throw new BadRepresentationException("Setting raw properties is not supported");
        }));
    }
    
    @Override
    public BackendKind getBackendKind () {
        return BackendKind.kUnknown;
    }
    
    @Override
    public boolean isPublished () {
        return true;
    }
    
    @Override
    public void update () {
        // RCTSendableBuilder automatically updates
    }
    
    @Override
    public void clearProperties () {
        map.clear();
    }
    
    private static class RCTField {
        private final Supplier<String> getter;
        private final FieldSetter setter;
        private RCTField (Supplier<String> getter, FieldSetter setter) {
            this.getter = getter;
            this.setter = setter;
        }
    }
    
    @FunctionalInterface
    private static interface FieldSetter {
        public void setField (String newValue) throws BadRepresentationException;
    }
    
    public static class BadRepresentationException extends Exception {
        public BadRepresentationException (String msg) {
            super(msg);
        }
    }
    
}
