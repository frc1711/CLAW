package claw.rct.commands;

import claw.rct.commands.CommandLineInterpreter.CommandLineException;
import claw.rct.network.low.ConsoleManager;

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
        this.helpMessage = new HelpMessage(usage, helpDescription);
        this.function = function;
    }
    
    /**
     * A help message for a particular {@link CommandProcessor}.
     */
    public class HelpMessage {
        
        public final String usage, helpDescription;
        
        private HelpMessage (String usage, String helpDescription) {
            this.usage = usage;
            this.helpDescription = helpDescription;
        }
        
        public String getCommand () {
            return command;
        }
        
    }
    
    /**
     * A functional interface which processes a command, possibly throwing a {@link BadArgumentsException}.
     */
    @FunctionalInterface
    public static interface CommandFunction {
        public void process (ConsoleManager console, Command cmd) throws BadArgumentsException;
    }
    
    /**
     * An exception thrown by {@link CommandProcessor}. Its message should be printed to the terminal.
     */
    public static class BadArgumentsException extends CommandLineException {
        
        private final String msg;
        
        /**
         * Creates a new {@link BadArgumentsException}, not customized for any command processor in particular.
         * Note that {@link CommandFunction}s need not modify any {@code BadArgumentException}s to include information
         * about the {@link CommandProcessor}, as this extra information is added by the {@link CommandLineInterpreter}.
         * @param msg A message describing the mistake made in the command's arguments.
         */
        public BadArgumentsException (String msg) {
            this(msg, null);
        }
        
        /**
         * Constructs a new {@link BadArgumentsException} with a given string describing its usage,
         * and a string describing the additional message to be displayed.
         * @param msg       A message describing the mistake made in the command's arguments.
         * @param processor The {@link CommandProcessor} which threw this exception.
         */
        private BadArgumentsException (String msg, CommandProcessor processor) {
            super("Bad command arguments. " + msg + (processor == null ? "" : ("\nUsage: " + processor.helpMessage.usage)));
            this.msg = msg;
        }
        
        /**
         * Gets a new, identical {@link BadArgumentsException}, except for the message being
         * customized for a particular command processor.
         * @param processor The command processor which threw this exception.
         * @return          The new {@link BadArgumentsException}.
         */
        public BadArgumentsException forCommandProcessor (CommandProcessor processor) {
            return new BadArgumentsException(msg, processor);
        }
        
    }
    
    /**
     * A extension of a {@link BadArgumentsException} which can be used in coordination with
     * {@link CommandLineInterpreter#checkNumArgs(String, int, int)} or {@link CommandLineInterpreter#checkNumArgs(String, int, int, int)}
     * to easily throw exceptions indicating that an incorrect number of arguments were passed to the command processor.
     */
    public static class IncorrectNumArgsException extends BadArgumentsException {
        /**
         * Use this constructor when the number of arguments passed to the command should fall within a range.
         */
        public IncorrectNumArgsException (int minArgs, int maxArgs, int receivedArgs) {
            this(minArgs+" to "+maxArgs, receivedArgs);
        }
        
        /**
         * Use this constructor when there is a precise number of arguments that passed to the command.
         */
        public IncorrectNumArgsException (int requiredArgs, int receivedArgs) {
            this(Integer.toString(requiredArgs), receivedArgs);
        }
        
        private IncorrectNumArgsException (String requiredArgs, int receivedArgs) {
            super("Received "+receivedArgs+" argument(s) but required "+requiredArgs+".");
        }
    }
    
    /**
     * Checks whether the number of arguments {@code receivedArgs} falls within the given maximum and minimum,
     * throwing an {@link IncorrectNumArgsException} if out of range.
     * @param minArgs                      The minimum number of arguments that should be passed to the command.
     * @param maxArgs                      The maximum number of arguments that should be passed to the command.
     * @param receivedArgs                 The number of arguments that were passed to the command.
     * @throws IncorrectNumArgsException
     */
    public static void checkNumArgs (int minArgs, int maxArgs, int receivedArgs) throws IncorrectNumArgsException {
        if (receivedArgs < minArgs || receivedArgs > maxArgs)
            throw new IncorrectNumArgsException(minArgs, maxArgs, receivedArgs);
    }
    
    /**
     * Checks whether the number of arguments {@code receivedArgs} is correct, throwing an {@link IncorrectNumArgsException} if it is not.
     * @param requiredArgs                  The number of arguments that should be passed to the command.
     * @param receivedArgs                  The number of arguments that were passed to the command.
     * @throws IncorrectNumArgsException
     */
    public static void checkNumArgs (int requiredArgs, int receivedArgs) throws IncorrectNumArgsException {
        if (receivedArgs != requiredArgs)
            throw new IncorrectNumArgsException(requiredArgs, receivedArgs);
    }
    
    /**
     * Checks whether a string argument is one of several options, throwing a {@link BadArgumentsException} if it is not.
     * @param argName                   The name to use to refer to the particular argument when explaining the issue.
     * @param argReceived               The string argument passed in.
     * @param argOptions                The list of possible valid arguments (varargs).
     * @throws BadArgumentsException
     */
    public static void expectedOneOf (
            String argName,
            String argReceived,
            String ...argOptions)
            throws BadArgumentsException {
        
        // Look through argument options to see if any match
        for (String argOption : argOptions)
            if (argOption.equals(argReceived)) return;
        
        // The method hasn't returned yet, so not argument options matched
        throw new BadArgumentsException(
            "'"+argName+"' argument must be one of: " + String.join(", ", argOptions) + " but '"+argReceived+"' was received.");
    }
    
}
