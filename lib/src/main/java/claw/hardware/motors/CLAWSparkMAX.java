package claw.hardware.motors;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.revrobotics.CANSparkMax;
import com.revrobotics.REVLibError;
import com.revrobotics.CANSparkMax.FaultID;
import com.revrobotics.CANSparkMax.IdleMode;

public class CLAWSparkMAX {
    
    private final CANSparkMax spark;
    
    public CLAWSparkMAX (CANSparkMax spark, SparkConfiguration... configurations) {
        this.spark = spark;
        burnConfigurations(configurations);
    }
    
    public Set<FaultID> getFaults () {
        return filterFaults(spark::getFault);
    }
    
    public Set<FaultID> getStickyFaults () {
        return filterFaults(spark::getStickyFault);
    }
    
    public void burnConfigurations (SparkConfiguration... configurations) {
        boolean hasConfiguredAny = false;
        for (SparkConfiguration config : configurations) {
            if (!config.isConfigSet(spark)) {
                hasConfiguredAny = true;
                config.setConfig(spark);
            }
        }
        
        if (hasConfiguredAny) {
            spark.burnFlash();
        }
    }
    
    private static Set<FaultID> filterFaults (Function<FaultID, Boolean> doesFaultExist) {
        HashSet<FaultID> faults = new HashSet<>();
        for (FaultID fault : FaultID.values()) {
            if (doesFaultExist.apply(fault)) faults.add(fault);
        }
        
        return faults;
    }
    
    public static interface SparkConfiguration {
        
        public boolean isConfigSet (CANSparkMax spark);
        public REVLibError setConfig (CANSparkMax spark);
        
        public static SparkConfiguration SetIdleMode (IdleMode idleMode) {
            return from(spark -> spark.getIdleMode().equals(idleMode), spark -> spark.setIdleMode(idleMode));
        }
        
        public static SparkConfiguration SetSoftLimits (IdleMode idleMode) {
            CANSparkMax s = null;
            s.setSoftLimit(null, 0)
            return from(spark -> spark.getIdleMode().equals(idleMode), spark -> spark.setIdleMode(idleMode));
        }
        
        public static SparkConfiguration from (Function<CANSparkMax, Boolean> isConfigSet, Function<CANSparkMax, REVLibError> setConfig) {
            return new SparkConfiguration() {
                @Override
                public boolean isConfigSet (CANSparkMax spark) {
                    return isConfigSet.apply(spark);
                }
                
                @Override
                public REVLibError setConfig (CANSparkMax spark) {
                    return setConfig.apply(spark);
                }
            };
        }
        
    }
    
}
