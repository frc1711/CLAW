package claw.telemetry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.wpi.first.util.sendable.SendableBuilder;

public class TelemetryRunner {
    
    /**
     * Add all fields and methods marked with the {@link Telemetry} annotation to a {@link SendableBuilder} to
     * more easily add properties to a SendableBuilder.
     * @param builder
     * @param telemetryObject
     */
    public static void addTelemetryFields(SendableBuilder builder, Object telemetryObject) {
        // Get the class of the telemetryObject which we will be using to add telemetry fields to
        Class<?> objClass = telemetryObject.getClass();
        
        // Use getDeclaredMethods to access private methods
        for (Method method : objClass.getDeclaredMethods()) {
            // Grab the annotation for the method if it exists
            Telemetry annotation = method.getAnnotation(Telemetry.class);
            if (annotation == null) continue;
            
            // Add the method as a property on the sendable builder
            addMethodProperty(builder, annotation.name(), method, telemetryObject);
        }
        
        // Use getDeclaredFields to access private fields
        for (Field field : objClass.getDeclaredFields()) {
            // Grab the annotation for the field if it exists
            Telemetry annotation = field.getAnnotation(Telemetry.class);
            if (annotation == null) continue;
            
            // Add the field as a property on the sendable builder
            addFieldProperty(builder, annotation.name(), field, telemetryObject);
        }
        
    }
    
    private static void addFieldProperty (SendableBuilder builder, String propertyName, Field field, Object telemetryObject) {
        
        if (Modifier.isStatic(field.getModifiers())) return;
        field.setAccessible(true);
        
        addProperty(
            builder,
            () -> {
                try {
                    return field.get(telemetryObject);
                } catch (Exception e) {
                    return null;
                }
            }, Optional.of(value -> {
                try {
                    field.set(telemetryObject, value);
                } catch (Exception e) { }
            }),
            field.getType(),
            propertyName
        );
        
    }
    
    private static void addMethodProperty (SendableBuilder builder, String propertyName, Method method, Object telemetryObject) {
        
        if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0 || method.getReturnType() == null) return;
        method.setAccessible(true);
        
        addProperty(
            builder,
            () -> {
                try {
                    return method.invoke(telemetryObject);
                } catch (IllegalAccessException e) {
                    return null;
                } catch (IllegalArgumentException e) {
                    return null;
                } catch (InvocationTargetException e) {
                    return null;
                }
            },
            Optional.empty(),
            method.getReturnType(),
            propertyName
        );
        
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T withDefault (Supplier<Object> supplier, T defaultValue, Class<T> cls) {
        Object value = supplier.get();
        if (value == null || !cls.isInstance(value)) return defaultValue;
        return (T)value;
    }
    
    private static <T> void addProperty (SendableBuilder builder, Supplier<Object> dataSupplier, Optional<Consumer<Object>> dataConsumer, Class<T> dataType, String propertyName) {
        
        // Builder supports boolean, double, float, long, String, plus arrays for each and a raw byte[] type
        
        if (Boolean.TYPE.isAssignableFrom(dataType)) {                                           // Single object types
            
            builder.addBooleanProperty(
                propertyName,
                () -> (boolean)withDefault(dataSupplier, false, Boolean.TYPE),
                v -> dataConsumer.ifPresent(c -> c.accept(v))
            );
            
        } else if (Double.TYPE.isAssignableFrom(dataType)) {
            
            builder.addDoubleProperty(
                propertyName,
                () -> (double)withDefault(dataSupplier, 0., Double.TYPE),
                v -> dataConsumer.ifPresent(c -> c.accept(v))
            );
            
        } else if (Float.TYPE.isAssignableFrom(dataType)) {
            
            builder.addFloatProperty(
                propertyName,
                () -> (float)withDefault(dataSupplier, 0f, Float.TYPE),
                v -> dataConsumer.ifPresent(c -> c.accept(v))
            );
            
        } else if (Long.TYPE.isAssignableFrom(dataType)) {
            
            builder.addIntegerProperty(
                propertyName,
                () -> (long)withDefault(dataSupplier, 0l, Long.TYPE),
                v -> dataConsumer.ifPresent(c -> c.accept(v))
            );
            
        } else if (String.class.isAssignableFrom(dataType)) {
            
            builder.addStringProperty(
                propertyName,
                () -> (String)withDefault(dataSupplier, "", String.class),
                v -> dataConsumer.ifPresent(c -> c.accept(v))
            );
            
        } else {
            
            builder.addStringProperty(propertyName, () -> "Unsupported Type: " + dataType.getName(), null);
            
        }
        
    }
    
    private TelemetryRunner() {}
    
}
