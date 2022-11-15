package rct.low;

import java.util.HashMap;

public abstract class InstructionMessage extends Message {
    
    public static final long serialVersionUID = 2L;
    
    public static class ConfigList extends InstructionMessage {
        public ConfigList () { }
    }
    
    public static class ConfigSet extends InstructionMessage {
        public final HashMap<String, String> settings;
        public ConfigSet (HashMap<String, String> settings) {
            this.settings = settings;
        }
    }
    
    public static class DebugInfoGet extends InstructionMessage {
        public DebugInfoGet () { }
    }
    
    public static class StreamsSetEnabled extends InstructionMessage {
        public final HashMap<String, Boolean> streamsEnableSettings;
        public StreamsSetEnabled (HashMap<String, Boolean> streamsEnableSettings) {
            this.streamsEnableSettings = streamsEnableSettings;
        }
    }
    
    public static class StreamsList extends InstructionMessage {
        public StreamsList () { }
    }
    
}
