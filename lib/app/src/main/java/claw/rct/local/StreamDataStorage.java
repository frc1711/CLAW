package claw.rct.local;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import claw.rct.network.messages.StreamDataMessage;
import claw.rct.network.messages.StreamDataMessage.StreamData;

public class StreamDataStorage {
    
    private final List<StreamData> streamData = new ArrayList<StreamData>();
    private int streamDataBufferSizeOnLastAccess = 0;
    
    private final Set<Runnable> onReceiveNewData = new HashSet<Runnable>();
    
    public void acceptDataMessage (StreamDataMessage msg) {
        synchronized (streamData) {
            for (StreamData data : msg.streamData)
                streamData.add(data);
        }
        
        for (Runnable runnable : onReceiveNewData)
            runnable.run();
    }
    
    public StreamData[] getNewData () {
        List<StreamData> newData;
        synchronized (streamData) {
            newData = streamData.subList(streamDataBufferSizeOnLastAccess, streamData.size());
            streamDataBufferSizeOnLastAccess = streamData.size();
            return newData.toArray(new StreamData[0]);
        }
    }
    
    public void addOnReceiveDataListener (Runnable runnable) {
        onReceiveNewData.add(runnable);
    }
    
    public void removeOnReceiveDataListener (Runnable runnable) {
        onReceiveNewData.remove(runnable);
    }
    
}
