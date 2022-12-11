package rct.network.local;

import java.io.IOException;

import rct.network.low.DriverStationSocketHandler;
import rct.network.low.ResponseMessage;
import rct.network.messages.CommandOutputMessage;
import rct.network.messages.StreamDataMessage;

public class LocalMessageRouter {
    
    private final DriverStationSocketHandler socket;
    
    public LocalMessageRouter (int teamNum, int port) throws IOException {
        socket = new DriverStationSocketHandler(teamNum, port, this::receiveMessage, this::handleSocketException);
    }
    
    private void receiveMessage (ResponseMessage msg) {
        Class<?> msgClass = msg.getClass();
        
        if (msgClass == CommandOutputMessage.class)
            receiveCommandOutputMessage((CommandOutputMessage)msg);
        
        else if (msgClass == StreamDataMessage.class)
            receiveStreamDataMessage((StreamDataMessage)msg);
    }
    
    private void receiveCommandOutputMessage (CommandOutputMessage msg) {
        
    }
    
    private void receiveStreamDataMessage (StreamDataMessage msg) {
        
    }
    
    private void handleSocketException (IOException e) {
        
    }
    
    public void close () throws IOException {
        socket.close();
    }
    
}
