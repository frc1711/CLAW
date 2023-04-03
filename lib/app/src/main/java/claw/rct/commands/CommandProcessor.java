package claw.rct.commands;

import java.io.Serializable;

import claw.actions.compositions.Context.TerminatedContextException;
import claw.rct.commands.CommandLineInterpreter.CommandLineException;
import claw.rct.console.ConsoleManager;

public class CommandProcessor {
    
    public final String command;
    public final HelpMessage helpMessage;
    public final CommandFunction function;
    
    /**
     * An object which can process a command and contains information about the nature of the command.
     * @param command           The name of the command which is processed by this {@link CommandProcessor}.
     * @param usage             A short string describing how the command
     * @param helpDescription
     * @param function
     */
    public CommandProcessor (String command, String usage, String helpDescription, CommandFunction function) {
        this.command = command;
        this.helpMessage = new HelpMessage(command, usage, helpDescription);
        this.function = function;
    }
    
    /**
     * A help message for a particular {@link CommandProcessor}.
     */
    public static record HelpMessage (String command, String usage, String helpDescription) implements Serializable { }
    
    /**
     * A functional interface which processes a command, possibly throwing a {@link BadCallException}.
     */
    @FunctionalInterface
    public static interface CommandFunction {
        public void process (ConsoleManager console, CommandReader reader) throws BadCallException, TerminatedContextException;
    }
    
    /**
     * An exception thrown by a {@link CommandProcessor} if it decides the input provided from
     * the command line does not fit its required format. Its message should be printed to the terminal.
     */
    public static class BadCallException extends CommandLineException {
        
        private final String msg;
        
        /**
         * Creates a new {@link BadCallException}, not customized for any command processor in particular.
         * Note that {@link CommandFunction}s need not modify any {@code BadCallException}s to include information
         * about the {@link CommandProcessor}, as this extra information is added by the {@link CommandLineInterpreter}.
         * @param msg A message describing the mistake made in the command's arguments.
         */
        public BadCallException (String msg) {
            this(msg, null);
        }
        
        /**
         * Constructs a new {@link BadCallException} with a given string describing its usage,
         * and a string describing the additional message to be displayed.
         * @param msg       A message describing the mistake made in the command's arguments.
         * @param processor The {@link CommandProcessor} which threw this exception.
         */
        private BadCallException (String msg, CommandProcessor processor) {
            super(msg + (processor == null ? "" : ("\nUsage: " + processor.helpMessage.usage)));
            this.msg = msg;
        }
        
        /**
         * Gets a new, identical {@link BadCallException}, except for the message being
         * customized for a particular command processor.
         * @param processor The command processor which threw this exception.
         * @return          The new {@link BadCallException}.
         */
        public BadCallException forCommandProcessor (CommandProcessor processor) {
            return new BadCallException(msg, processor);
        }
        
    }
    
}
