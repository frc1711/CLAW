package rct.commands.system;

import java.io.Serializable;
import java.util.HashMap;

public class SysDataFrame implements Serializable {
    
    public final String identifier;
    public final HashMap<String, SysDataFrame> subframes;
    public final HashMap<String, String> values;
    
    public SysDataFrame (String identifier, HashMap<String, SysDataFrame> subframes, HashMap<String, String> values) {
        this.identifier = identifier;
        this.subframes = subframes;
        this.values = values;
    }
    
}
