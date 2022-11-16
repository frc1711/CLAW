package rct.low;

public abstract class ResponseMessage extends Message {
    
    public static final long serialVersionUID = 1L;
    
    public ResponseMessage (int id) {
        super(id);
    }
    
    public static final class InstructionStatusResponse extends ResponseMessage {
        public final Status status;
        public InstructionStatusResponse (int id, Status status) {
            super(id);
            this.status = status;
        }
    }
    
    public static enum Status {
        SUCCESS,
        FAILURE,
    }
    
}
