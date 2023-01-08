package claw.internal.rct.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import claw.internal.rct.network.messages.LogDataMessage;
import claw.internal.rct.network.messages.LogDataMessage.LogData;

public class LogDataStorage {
    
    // TODO: Connect a robot connection watcher to the log data storage
    
    private final List<LogData> logData = new ArrayList<LogData>();
    
    private final Set<Consumer<LogData[]>> onReceiveNewData = new HashSet<Consumer<LogData[]>>();
    
    public void acceptDataMessage (LogDataMessage msg) {
        synchronized (logData) {
            for (LogData data : msg.logData)
                logData.add(data);
        }
        
        for (Consumer<LogData[]> receiveNewData: onReceiveNewData)
            receiveNewData.accept(Arrays.copyOf(msg.logData, msg.logData.length));
    }
    
    public void addOnReceiveDataListener (Consumer<LogData[]> listener) {
        onReceiveNewData.add(listener);
    }
    
    public void removeOnReceiveDataListener (Consumer<LogData[]> listener) {
        onReceiveNewData.remove(listener);
    }
    
}
