package rct.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rct.network.messages.StreamDataMessage;
import rct.network.messages.StreamDataMessage.StreamData;

public class StreamDataStorage {
    
    private HashMap<String, List<StreamData>> streams;
    
    public void acceptDataMessage (StreamDataMessage msg) {
        for (StreamData data : msg.streamData)
            addStreamData(data);
    }
    
    private void addStreamData (StreamData data) {
        String streamName = data.streamName;
        
        if (streams.containsKey(streamName))
            streams.get(streamName).add(data);
        else
            streams.put(streamName, new ArrayList<StreamData>());
    }
    
}
