package claw.rct.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import claw.rct.network.messages.StreamDataMessage;
import claw.rct.network.messages.StreamDataMessage.StreamData;

public class StreamDataStorage {
    
    // TODO: Connect a robot connection watcher to the stream data storage
    
    private final List<StreamData> streamData = new ArrayList<StreamData>();
    
    private final Set<Consumer<StreamData[]>> onReceiveNewData = new HashSet<Consumer<StreamData[]>>();
    
    public void acceptDataMessage (StreamDataMessage msg) {
        synchronized (streamData) {
            for (StreamData data : msg.streamData)
                streamData.add(data);
        }
        
        for (Consumer<StreamData[]> receiveNewData: onReceiveNewData)
            receiveNewData.accept(Arrays.copyOf(msg.streamData, msg.streamData.length));
    }
    
    public void addOnReceiveDataListener (Consumer<StreamData[]> listener) {
        onReceiveNewData.add(listener);
    }
    
    public void removeOnReceiveDataListener (Consumer<StreamData[]> listener) {
        onReceiveNewData.remove(listener);
    }
    
}
