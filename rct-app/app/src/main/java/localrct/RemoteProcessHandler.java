package localrct;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import claw.rct.base.console.ConsoleManager;
import claw.rct.base.console.ConsoleManager.TerminalKilledException;
import claw.rct.base.network.low.InstructionMessage;
import claw.rct.base.network.low.concurrency.KeepaliveWatcher;
import claw.rct.base.network.messages.commands.CommandInputMessage;
import claw.rct.base.network.messages.commands.CommandOutputMessage;
import claw.rct.base.network.messages.commands.ProcessKeepaliveLocal;
import claw.rct.base.network.messages.commands.ProcessKeepaliveRemote;
import claw.rct.base.network.messages.commands.StartCommandMessage;
import claw.rct.base.network.messages.commands.CommandOutputMessage.ConsoleManagerOperation;
import claw.rct.base.network.messages.commands.CommandOutputMessage.ConsoleManagerRequest;

public class RemoteProcessHandler {
    
    private final ConsoleManager console;
    private final Consumer<InstructionMessage> instructionSender;
    private final int processId;
    
    private final ConcurrentLinkedQueue<CommandOutputMessage> commandOutputQueue = new ConcurrentLinkedQueue<>();
    
    private final KeepaliveWatcher keepaliveWatcher;
    
    private boolean startedExecuting = false;
    private boolean terminated = false;
    private IOException terminateException = null;
    
    public RemoteProcessHandler (
            ConsoleManager console,
            Consumer<InstructionMessage> instructionSender,
            long keepaliveDuration,
            long keepaliveSendInterval,
            int processId) {
        this.console = console;
        this.instructionSender = instructionSender;
        this.processId = processId;
        this.keepaliveWatcher = new KeepaliveWatcher(
            keepaliveDuration,
            keepaliveSendInterval,
            this::sendKeepaliveMessage,
            () -> this.terminate(new IOException("Keepalive timed out")));
    }
    
    public void receiveCommandOutputMessage (CommandOutputMessage msg) {
        commandOutputQueue.add(msg);
        keepaliveWatcher.continueKeepalive();
    }
    
    public void receiveKeepalive (ProcessKeepaliveRemote msg) {
        keepaliveWatcher.continueKeepalive();
    }
    
    public void execute (String command) throws IOException, TerminalKilledException {
        // Do nothing if the remote process has already begun executing
        if (startedExecuting) return;
        startedExecuting = true;
        
        keepaliveWatcher.start();
        
        // Send the initial StartCommandMessage
        instructionSender.accept(new StartCommandMessage(processId, command));
        
        // Start the loop of receiving and responding to messages
        while (!terminated) {
            awaitCommandOutputLoop();
        }
        
        // If at any point the commandOutputObjectWaiter is killed, then the process must have
        // been terminated and we should silently exit the above block
        
        if (terminateException != null) throw terminateException;
    }
    
    private void awaitCommandOutputLoop () throws TerminalKilledException {
        // Waiting for an output message that matches the process ID
        Optional<CommandOutputMessage> outputMessage = Optional.empty();
        while (outputMessage.isEmpty() || outputMessage.get().commandProcessId != processId) {
            while (commandOutputQueue.isEmpty()) {
                Thread.onSpinWait();
            }
            
            outputMessage = Optional.of(commandOutputQueue.poll());
        }
        
        CommandOutputMessage message = outputMessage.get();
        
        // Once the output message has been received, process the operations
        for (int i = 0; i < message.operations.length; i ++)
            processConsoleManagerOperation(message.operations[i]);
        
        // Send an input message if requested by the output message
        sendRespondingInputMessage(message);
        
        // Terminate the command if requested by the output message
        if (message.terminateCommand) terminate();
    }
    
    private void processConsoleManagerOperation (ConsoleManagerOperation operation) throws TerminalKilledException {
        switch (operation.operationType) {
            case CLEAR:
                console.clear();
                break;
            case CLEAR_LINE:
                console.clearLine();
                break;
            case CLEAR_WAITING_INPUT_LINES:
                console.clearWaitingInputLines();
                break;
            case FLUSH:
                console.flush();
                break;
            case MOVE_UP:
                console.moveUp(operation.moveUp_lines);
                break;
            case SAVE_CURSOR_POS:
                console.saveCursorPos();
                break;
            case RESTORE_CURSOR_POS:
                console.restoreCursorPos();
                break;
            case PRINT:
                if (operation.print_message != null)
                    console.print(operation.print_message);
                break;
            case PRINT_ERR:
                if (operation.print_message != null)
                    console.printErr(operation.print_message);
                break;
            case PRINT_SYS:
                if (operation.print_message != null)
                    console.printSys(operation.print_message);
                break;
        }
    }
    
    private void sendRespondingInputMessage (CommandOutputMessage msg) throws TerminalKilledException {
        switch (msg.request) {
            case NO_REQUEST:
                break;
            case HAS_INPUT_READY:
                instructionSender.accept(
                    new CommandInputMessage(
                        processId,
                        console.hasInputReady(),
                        null,
                        ConsoleManagerRequest.HAS_INPUT_READY));
                break;
            case READ_INPUT_LINE:
                instructionSender.accept(
                    new CommandInputMessage(
                        processId,
                        false,
                        console.readInputLine(),
                        ConsoleManagerRequest.READ_INPUT_LINE));
                break;
        }
    }
    
    public void terminate () {
        terminate(null);
    }
    
    public void terminate (IOException exception) {
        // Do nothing if the remote process is not executing
        if (!isExecuting()) return;
        terminateException = exception;
        terminated = true;
        
        keepaliveWatcher.stopWatching();
    }
    
    private void sendKeepaliveMessage () {
        instructionSender.accept(new ProcessKeepaliveLocal());
    }
    
    public boolean hasStartedExecution () {
        return startedExecuting;
    }
    
    public boolean hasBeenTerminated () {
        return terminated;
    }
    
    public boolean isExecuting () {
        return startedExecuting && !terminated;
    }
    
}
