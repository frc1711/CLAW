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
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import claw.api.subsystems.SubsystemCLAW;
import claw.internal.rct.network.low.ConsoleManager;
import edu.wpi.first.util.function.BooleanConsumer;
import edu.wpi.first.util.function.FloatConsumer;
import edu.wpi.first.util.function.FloatSupplier;
import edu.wpi.first.util.sendable.SendableBuilder;

public class RCTSendableBuilder implements SendableBuilder {
    
    private String dataTypeString = "";
    private boolean isActuator = false;
    private Runnable setSafeStateFunc = () -> { };
    private final Map<String, RCTField> map = new HashMap<String, RCTField>();
    private final List<AutoCloseable> closeables = new ArrayList<AutoCloseable>();
    
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
            throwForNullSetter(setter);
            
            str = str.toLowerCase();
            if (str.equals("true") || str.equals("t"))
                setter.accept(true);
            else if (str.equals("false") || str.equals("f"))
                setter.accept(false);
            else
                throw new BadRepresentationException("Did not receive a valid boolean representation.");
        }));
    }
    
    @Override
    public void addIntegerProperty (String key, LongSupplier getter, LongConsumer setter) {
        addField(key, new RCTField(() -> String.valueOf(getter.getAsLong()), str -> {
            throwForNullSetter(setter);
            
            try {
                setter.accept(Long.parseLong(str));
            } catch (NumberFormatException e) {
                throw new BadRepresentationException("Did not receive a valid long integer representation.");
            }
        }));
    }
    
    @Override
    public void addDoubleProperty (String key, DoubleSupplier getter, DoubleConsumer setter) {
        addField(key, new RCTField(() -> String.valueOf(getter.getAsDouble()), str -> {
            throwForNullSetter(setter);
            
            try {
                setter.accept(Double.parseDouble(str));
            } catch (NumberFormatException e) {
                throw new BadRepresentationException("Did not receive a valid double representation.");
            }
        }));
    }
    
    @Override
    public void addFloatProperty (String key, FloatSupplier getter, FloatConsumer setter) {
        addField(key, new RCTField(() -> String.valueOf(getter.getAsFloat()), str -> {
            throwForNullSetter(setter);
            
            try {
                setter.accept(Float.parseFloat(str));
            } catch (NumberFormatException e) {
                throw new BadRepresentationException("Did not receive a valid float representation.");
            }
        }));
    }
    
    @Override
    public void addStringProperty (String key, Supplier<String> getter, Consumer<String> setter) {
        addField(key, new RCTField(getter, str -> setter.accept(str)));
    }
    
    @Override
    public void addBooleanArrayProperty (String key, Supplier<boolean[]> getter, Consumer<boolean[]> setter) {
        addArrayProperty(key, "boolean", () -> getter.get().length);
    }
    
    @Override
    public void addIntegerArrayProperty (String key, Supplier<long[]> getter, Consumer<long[]> setter) {
        addArrayProperty(key, "long", () -> getter.get().length);
    }
    
    @Override
    public void addDoubleArrayProperty (String key, Supplier<double[]> getter, Consumer<double[]> setter) {
        addArrayProperty(key, "double", () -> getter.get().length);
    }
    
    @Override
    public void addFloatArrayProperty (String key, Supplier<float[]> getter, Consumer<float[]> setter) {
        addArrayProperty(key, "float", () -> getter.get().length);
    }
    
    @Override
    public void addStringArrayProperty (String key, Supplier<String[]> getter, Consumer<String[]> setter) {
        addArrayProperty(key, "String", () -> getter.get().length);
    }
    
    @Override
    public void addRawProperty (String key, String typeString, Supplier<byte[]> getter, Consumer<byte[]> setter) {
        addArrayProperty(key, typeString+": byte", () -> getter.get().length);
    }
    
    private void addArrayProperty (String key, String typeString, LongSupplier length) {
        addField(key, new RCTField(() -> (typeString+"["+length.getAsLong()+"]"), str -> {
            throw new BadRepresentationException("Setting array properties is not supported");
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
    
    @Override
    public void close () throws Exception {
        for (AutoCloseable closeable : closeables)
            closeable.close();
    }
    
    @Override
    public void addCloseable (AutoCloseable closeable) {
        closeables.add(closeable);
    }
    
    private void throwForNullSetter (Object setter) throws BadRepresentationException {
        if (setter == null)
            throw new BadRepresentationException("This field does not support mutation.");
    }
    
    private static class RCTField {
        private final Supplier<String> getter;
        private final FieldSetter<String> setter;
        private RCTField (Supplier<String> getter, FieldSetter<String> setter) {
            this.getter = getter;
            this.setter = setter;
        }
    }
    
    @FunctionalInterface
    private static interface FieldSetter<T> {
        public void setField (T newValue) throws BadRepresentationException;
    }
    
    public static class BadRepresentationException extends Exception {
        public BadRepresentationException (String msg) {
            super(msg);
        }
    }
    
}
