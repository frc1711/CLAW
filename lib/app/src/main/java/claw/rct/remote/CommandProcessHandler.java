package claw.rct.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import claw.rct.network.low.ConsoleManager;
import claw.rct.network.low.concurrency.KeepaliveWatcher;
import claw.rct.network.low.ResponseMessage;
import claw.rct.network.low.concurrency.Waiter;
import claw.rct.network.low.concurrency.Waiter.NoValueReceivedException;
import claw.rct.network.messages.commands.CommandInputMessage;
import claw.rct.network.messages.commands.CommandOutputMessage;
import claw.rct.network.messages.commands.ProcessKeepaliveLocal;
import claw.rct.network.messages.commands.ProcessKeepaliveRemote;
import claw.rct.network.messages.commands.CommandOutputMessage.ConsoleManagerOperation;
import claw.rct.network.messages.commands.CommandOutputMessage.ConsoleManagerOperationType;
import claw.rct.network.messages.commands.CommandOutputMessage.ConsoleManagerRequest;

public class CommandProcessHandler implements ConsoleManager {
    
    private final Consumer<ResponseMessage> responseSender;
    private final int processId;
    
    private final Waiter<String> readLineWaiter = new Waiter<String>();
    private final Waiter<Boolean> hasInputReadyWaiter = new Waiter<Boolean>();
    private final KeepaliveWatcher keepaliveWatcher;
    
    private final List<ConsoleManagerOperation> operations = new ArrayList<ConsoleManagerOperation>();
    
    private boolean isTerminated = false;
    
    public CommandProcessHandler (Consumer<ResponseMessage> responseSender, int processId, long keepaliveDuration, long keepaliveSendInterval) {
        this.responseSender = responseSender;
        this.processId = processId;
        
        // Start the keepalive thread, watching for keepalive messages, killing the process if none are received
        keepaliveWatcher = new KeepaliveWatcher(
            keepaliveDuration,
            keepaliveSendInterval,
            this::sendKeepaliveMessage,
            () -> terminate(false));
        
        keepaliveWatcher.start();
    }
    
    private void sendKeepaliveMessage () {
        responseSender.accept(new ProcessKeepaliveRemote());
    }
    
    // Receiving messages
    
    public void receiveKeepaliveMessage (ProcessKeepaliveLocal msg) {
        keepaliveWatcher.continueKeepalive();
    }
    
    public void receiveCommandInputMessage (CommandInputMessage msg) {
        // Do nothing if the process IDs do not match
        if (msg.commandProcessId != processId) return;
        
        keepaliveWatcher.continueKeepalive();
        
        // Send data to the request waiters depending on the input message
        switch (msg.request) {
            case NO_REQUEST:
                break;
            case HAS_INPUT_READY:
                hasInputReadyWaiter.receive(msg.hasInputReady);
                break;
            case READ_INPUT_LINE:
                readLineWaiter.receive(msg.inputLine);
                break;
        }
    }
    
    // Control methods
    
    public void terminate (boolean flushOutput) {
        // Do nothing if the process is terminated
        if (isTerminated) return;
        isTerminated = true;
        
        if (flushOutput) {
            flushOperationsBuffer(ConsoleManagerRequest.NO_REQUEST);
        }
        
        // Kill all request waiters because the process no longer exists
        readLineWaiter.kill();
        hasInputReadyWaiter.kill();
        
        // Stop the keepalive watcher
        keepaliveWatcher.stopWatching();
    }
    
    // ConsoleManager methods
    
    @Override
    public void print (String msg) {
        // Add the print operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.PRINT, 0, msg));
    }
    
    @Override
    public void printErr (String msg) {
        // Add the printErr operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.PRINT_ERR, 0, msg));
    }
    
    @Override
    public void printSys (String msg) {
        // Add the printSys operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.PRINT_SYS, 0, msg));
    }
    
    @Override
    public void clear () {
        // Add the clear operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.CLEAR, 0, null));
    }
    
    @Override
    public void moveUp (int lines) {
        // Add the moveUp operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.MOVE_UP, lines, null));
    }
    
    @Override
    public void clearLine () {
        // Add the clearLine operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.CLEAR_LINE, 0, null));
    }
    
    @Override
    public void saveCursorPos () {
        // Add the saveCursorPos operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.SAVE_CURSOR_POS, 0, null));
    }
    
    @Override
    public void restoreCursorPos () {
        // Add the restoreCursorPos operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.RESTORE_CURSOR_POS, 0, null));
    }
    
    @Override
    public void clearWaitingInputLines () {
        // Add the restoreCursorPos operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.CLEAR_WAITING_INPUT_LINES, 0, null));
    }
    
    @Override
    public void flush () {
        // Add the flush operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.FLUSH, 0, null));
        
        // Also flush the output buffer to send all operations to local
        flushOperationsBuffer(ConsoleManagerRequest.NO_REQUEST);
    }
    
    @Override
    public boolean hasInputReady () {
        try {
            if (isTerminated)
                throw new TerminatedProcessException();
            
            // Send operations buffer to local along with the hasInputReady request
            flushOperationsBuffer(ConsoleManagerRequest.HAS_INPUT_READY);
            
            // Return the value received by the hasInputReadyWaiter
            return hasInputReadyWaiter.waitForValue();
        } catch (NoValueReceivedException e) {
            // If the waiter was killed then the process must have been terminated
            throw new TerminatedProcessException();
        }
    }
    
    @Override
    public String readInputLine () {
        try {
            if (isTerminated)
                throw new TerminatedProcessException();
            
            // Send operations buffer to local along with the readInputLine request
            flushOperationsBuffer(ConsoleManagerRequest.READ_INPUT_LINE);
            
            // Return the value received by the readLineWaiter
            return readLineWaiter.waitForValue();
        } catch (NoValueReceivedException e) {
            // If the waiter was killed then the process must have been terminated
            throw new TerminatedProcessException();
        }
    }
    
    // Private control methods
    
    private void flushOperationsBuffer (ConsoleManagerRequest request) {
        // Copy contents of the operations list into an array and clear the list
        ConsoleManagerOperation[] operationsArray;
        synchronized (operations) {
            operationsArray = operations.toArray(new ConsoleManagerOperation[0]);
            operations.clear();
        }
        
        // Send a response based on the set request and the list of operations to perform
        // This is done outside the synchronized block so we don't take control of the operations
        // list for longer than necessary
        responseSender.accept(new CommandOutputMessage(processId, isTerminated, request, operationsArray));
    }
    
    private void addOperation (ConsoleManagerOperation operation) {
        // Throw an exception if the process is terminated
        if (isTerminated)
            throw new TerminatedProcessException();
        
        // Add the operation to the buffer (synchronized for thread safety)
        synchronized (operations) {
            operations.add(operation);
        }
    }
    
    /**
     * A {@code RuntimeException} which can be thrown by a {@link CommandProcessHandler} for certain methods if called
     * after the process was terminated.
     */
    public static class TerminatedProcessException extends RuntimeException {
        public TerminatedProcessException () {
            super("Process has been terminated");
        }
    }
    
}
