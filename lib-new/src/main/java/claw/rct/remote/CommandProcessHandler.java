package claw.rct.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import claw.rct.console.ConsoleManager;
import claw.rct.network.low.concurrency.KeepaliveWatcher;
import claw.rct.network.low.concurrency.SignalWaiter;
import claw.rct.network.low.ResponseMessage;
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
    
    private final SignalWaiter<String> readLineWaiter = new SignalWaiter<String>();
    private final SignalWaiter<Boolean> hasInputReadyWaiter = new SignalWaiter<Boolean>();
    private final KeepaliveWatcher keepaliveWatcher;
    
    private final List<ConsoleManagerOperation> operations = new ArrayList<ConsoleManagerOperation>();
    
    private boolean terminated = false;
    
    public CommandProcessHandler (Consumer<ResponseMessage> responseSender, int processId, long keepaliveDuration, long keepaliveSendInterval) {
        this.responseSender = responseSender;
        this.processId = processId;
        
        // Start the keepalive thread, watching for keepalive messages, killing the process if none are received
        keepaliveWatcher = new KeepaliveWatcher(
            keepaliveDuration,
            keepaliveSendInterval,
            this::sendKeepaliveMessage,
            this::terminate
        );
        
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
        
        // Send data to the request ObjectWaiters depending on the input message
        switch (msg.request) {
            case NO_REQUEST:
                break;
            case HAS_INPUT_READY:
                hasInputReadyWaiter.receiveSignal(msg.hasInputReady);
                break;
            case READ_INPUT_LINE:
                readLineWaiter.receiveSignal(msg.inputLine);
                break;
        }
    }
    
    // Control methods
    
    @Override
    public void terminate () {
        // Do nothing if the process is terminated
        if (isTerminated()) return;
        terminated = true;
        
        // Flush the operations buffer to let the client side know we've terminated the process
        flushOperationsBuffer(ConsoleManagerRequest.NO_REQUEST);
        
        // Kill all request ObjectWaiters because the process no longer exists
        readLineWaiter.kill();
        hasInputReadyWaiter.kill();
        
        // Stop the keepalive watcher
        keepaliveWatcher.stopWatching();
    }
    
    @Override
    public boolean isTerminated () {
        return terminated;
    }
    
    // ConsoleManager methods
    
    @Override
    public synchronized void print (String msg) throws TerminatedContextException {
        // Add the print operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.PRINT, 0, msg));
    }
    
    @Override
    public synchronized void printErr (String msg) throws TerminatedContextException {
        // Add the printErr operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.PRINT_ERR, 0, msg));
    }
    
    @Override
    public synchronized void printSys (String msg) throws TerminatedContextException {
        // Add the printSys operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.PRINT_SYS, 0, msg));
    }
    
    @Override
    public synchronized void clear () throws TerminatedContextException {
        // Add the clear operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.CLEAR, 0, null));
    }
    
    @Override
    public synchronized void moveUp (int lines) throws TerminatedContextException {
        // Add the moveUp operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.MOVE_UP, lines, null));
    }
    
    @Override
    public synchronized void clearLine () throws TerminatedContextException {
        // Add the clearLine operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.CLEAR_LINE, 0, null));
    }
    
    @Override
    public synchronized void saveCursorPos () throws TerminatedContextException {
        // Add the saveCursorPos operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.SAVE_CURSOR_POS, 0, null));
    }
    
    @Override
    public synchronized void restoreCursorPos () throws TerminatedContextException {
        // Add the restoreCursorPos operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.RESTORE_CURSOR_POS, 0, null));
    }
    
    @Override
    public synchronized void clearWaitingInputLines () throws TerminatedContextException {
        // Add the restoreCursorPos operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.CLEAR_WAITING_INPUT_LINES, 0, null));
    }
    
    @Override
    public synchronized void flush () throws TerminatedContextException {
        // Add the flush operation
        addOperation(new ConsoleManagerOperation(ConsoleManagerOperationType.FLUSH, 0, null));
        
        // Also flush the output buffer to send all operations to local
        flushOperationsBuffer(ConsoleManagerRequest.NO_REQUEST);
    }
    
    @Override
    public synchronized boolean hasInputReady () throws TerminatedContextException {
        useContext();
        
        // Send operations buffer to local along with the hasInputReady request
        flushOperationsBuffer(ConsoleManagerRequest.HAS_INPUT_READY);
        
        // Return the value received by the hasInputReadyObjectWaiter
        Optional<Boolean> signalReceived = hasInputReadyWaiter.awaitSignal();
        if (signalReceived.isPresent()) {
            return signalReceived.get();
        }
        
        // If the waiter was killed then the process must have been terminated
        throw getTerminatedException();
        
    }
    
    @Override
    public synchronized String readInputLine () throws TerminatedContextException {
        
        useContext();
        
        // Send operations buffer to local along with the readInputLine request
        flushOperationsBuffer(ConsoleManagerRequest.READ_INPUT_LINE);
        
        // Return the value received by the readLineObjectWaiter
        Optional<String> signalReceived = readLineWaiter.awaitSignal();
        if (signalReceived.isPresent()) {
            return signalReceived.get();
        }
        
        // If the waiter was killed then the process must have been terminated
        throw getTerminatedException();
        
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
        responseSender.accept(new CommandOutputMessage(processId, isTerminated(), request, operationsArray));
    }
    
    private void addOperation (ConsoleManagerOperation operation) throws TerminatedContextException {
        // Throw an exception if the process is terminated
        useContext();
        
        // Add the operation to the buffer (synchronized for thread safety)
        synchronized (operations) {
            operations.add(operation);
        }
    }
    
}
