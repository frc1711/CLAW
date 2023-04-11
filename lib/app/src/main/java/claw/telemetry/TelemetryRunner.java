package claw.telemetry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.wpi.first.util.sendable.SendableBuilder;

public class TelemetryRunner {
    
    public static void addTelemetryFields(SendableBuilder builder, Object telemetryObject) {
        Class<?> objClass = telemetryObject.getClass();
        
        // for (Method method : objClass.getMethods()) {
        //     // Grab the annotation for the method if it exists
        //     Telemetry annotation = method.getAnnotation(Telemetry.class);
        //     if (annotation == null) continue;
            
        //     addMethodProperty(builder, method, annotation, telemetryObject);
        // }
        
        for (Field field : objClass.getFields()) {
            Telemetry annotation = field.getAnnotation(Telemetry.class);
            if (annotation == null) continue;
            
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
