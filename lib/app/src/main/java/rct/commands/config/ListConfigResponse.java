package rct.commands.config;

import java.util.HashMap;

import rct.low.ResponseMessage;

public class ListConfigResponse extends ResponseMessage {
    
    public final HashMap<String, ConfigValue> configurations;
    
    public ListConfigResponse (int id, HashMap<String, ConfigValue> configurations) {
        super(id);
        this.configurations = configurations;
    }
    
}
