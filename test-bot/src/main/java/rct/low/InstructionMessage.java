package rct.low;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

public abstract class InstructionMessage implements Serializable {
    
    public static final long serialVersionUID = 1L;
    
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
    
    public byte[] getData () {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bytesOut);
            out.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException("Exception reading InstructionMessage byte[] data:\n" + e);
        }
        
        return bytesOut.toByteArray();
    }
    
    public static InstructionMessage readFromStream (InputStream inputStream) throws IOException {
        ObjectInputStream objIn = new ObjectInputStream(inputStream);
        try {
            return (InstructionMessage)objIn.readObject();
        } catch (ClassCastException e) {
            throw new IOException("Read in an object that was not an InstructionMessage when an InstructionMessage was expected");
        } catch (ClassNotFoundException e) {
            throw new IOException("Read in an unidentifiable class when an InstructionMessage was expected");
        }
    }
    
}
