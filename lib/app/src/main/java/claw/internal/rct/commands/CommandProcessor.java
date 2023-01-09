package claw.internal.rct.commands;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import claw.internal.rct.commands.CommandLineInterpreter.CommandLineException;
import claw.internal.rct.network.low.ConsoleManager;

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
            super(msg + (processor == null ? "" : ("\nUsage: " + processor.helpMessage.usage)));
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
    
    public static void expectNothing (Command cmd) throws BadArgumentsException {
        expectNoArgs(cmd);
        expectNoOptions(cmd);
        expectNoFlags(cmd);
    }
    
    public static void expectNoOptions (Command cmd) throws BadArgumentsException {
        if (cmd.hasAnyOptions())
            throw new BadArgumentsException("Command takes no options.");
    }
    
    public static void expectNoFlags (Command cmd) throws BadArgumentsException {
        if (cmd.hasAnyFlags())
            throw new BadArgumentsException("Command takes no flags.");
    }
    
    public static void expectNoArgs (Command cmd) throws BadArgumentsException {
        if (cmd.argsLen() > 0)
            throw new BadArgumentsException("Command takes no arguments.");
    }
    
    public static void expectMaxArgs (Command cmd, int numArgs) throws BadArgumentsException {
        if (cmd.argsLen() > numArgs)
            throw new BadArgumentsException("Too many arguments.");
    }
    
    public static String expectString (Command cmd, String argName, int argIndex) throws BadArgumentsException {
        // If exactly one more argument was required, throw an error with the required argument name
        if (cmd.argsLen() == argIndex)
            throw new BadArgumentsException("Expected another argument: \""+argName+"\".");
        
        // If more than one more arg was required, put a more generic error message
        if (cmd.argsLen() <= argIndex)
            throw new BadArgumentsException("Expected more arguments.");
        
        // Return the argument string
        return cmd.getArg(argIndex);
    }
    
    public static int expectInt (Command cmd, String argName, int argIndex) throws BadArgumentsException {
        
        // Expect the string argument
        String arg = expectString(cmd, argName, argIndex);
        
        // Attempt to parse the argument to an int
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new BadArgumentsException("\""+argName+"\" argument must be an integer.");
        }
        
    }
    
    public static double expectDouble (Command cmd, String argName, int argIndex) throws BadArgumentsException {
        
        // Expect the string argument
        String arg = expectString(cmd, argName, argIndex);
        
        // Attempt to parse the argument to a double
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new BadArgumentsException("\""+argName+"\" argument must be a decimal number.");
        }
        
    }
    
    public static String expectOneOf (Command cmd, String argName, int argIndex, Collection<String> argOptions) throws BadArgumentsException {
        
        // Expect the string argument
        String arg = expectString(cmd, argName, argIndex);
        
        // Look through argument options to see if any match one of the given argument options
        Iterator<String> iter = argOptions.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(arg))
                return arg;
        }
        
        // No argument options matched
        if (argOptions.size() <= 4)
            throw new BadArgumentsException("\""+argName+"\" argument must be one of: " + String.join(", ", argOptions) + ", but \""+arg+"\" was received.");
        else
            throw new BadArgumentsException("\""+arg+"\" argument did not match any expected argument.");
        
    }
    
    public static String expectOneOf (Command cmd, String argName, int argIndex, String ...argOptions) throws BadArgumentsException {
        return expectOneOf(cmd, argName, argIndex, List.of(argOptions));
    }
    
}
