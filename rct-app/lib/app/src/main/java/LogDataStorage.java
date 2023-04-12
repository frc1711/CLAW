import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import claw.rct.network.messages.LogDataMessage;
import claw.rct.network.messages.LogDataMessage.LogData;

public class LogDataStorage {
    
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
