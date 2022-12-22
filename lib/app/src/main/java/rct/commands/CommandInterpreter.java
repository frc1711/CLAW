package rct.commands;

import java.util.HashMap;

/**
 * Represents a system for interpreting {@link Command}s. Recognized commands can be added via
 * {@link #addCommandConsumer(String, CommandProcessor)} and commands can be parsed and processed
 * via {@link #processLine(String)}.
 */
public class CommandInterpreter {
    
    private final HashMap<String, CommandProcessor> commandConsumers = new HashMap<String, CommandProcessor>();
    
    public CommandInterpreter () { }
    
    /**
     * Recognizes a new command name as valid. The command name is case insensitive.
     * @param command   The command's name. See {@link Command.ParseException} for
     * command line formatting.
     * @param receiver  A {@link ConsumerProcessor} which can accept a command and process it.
     */
    public void addCommandConsumer (String command, CommandProcessor receiver) {
        commandConsumers.put(command.toUpperCase(), receiver);
    }
    
    /**
     * Processes a line into a {@link Command} and attempts to call one of this interpreter's command consumers
     * on the command. See {@link Command.ParseException} for command line formatting.
     * @param line                      The command line string
     * @return                          Whether or not the command was recognized by this interpreter. Use
     * {@link #addCommandConsumer(String, Consumer)} to add more recognized commands.
     * @throws Command.ParseException   An exception thrown if the command is malformed
     */
    public boolean processLine (String line) throws Command.ParseException, BadArgumentsException {
        Command commandObj = new Command(line);
        String commandName = commandObj.getCommand().toUpperCase();
        
        // Send the command to its consumer if the command name exists in the commandConsumers hashmap,
        // otherwise throws an exception
        if (commandConsumers.containsKey(commandName)) {
            commandConsumers.get(commandName).accept(commandObj);
            return true;
        } else return false;
    }
    
    @FunctionalInterface
    public static interface CommandProcessor {
        public void accept (Command cmd) throws BadArgumentsException;
    }
    
    public static class BadArgumentsException extends Exception {
        public BadArgumentsException (String usage, String msg) {
            super("Bad command arguments. "+msg+"\nUsage: " + usage);
        }
    }
    
    public static class IncorrectNumArgsException extends BadArgumentsException {
        public IncorrectNumArgsException (String usage, int minArgs, int maxArgs, int receivedArgs) {
            this(usage, minArgs+" to "+maxArgs, receivedArgs);
        }
        
        public IncorrectNumArgsException (String usage, int requiredArgs, int receivedArgs) {
            this(usage, Integer.toString(requiredArgs), receivedArgs);
        }
        
        private IncorrectNumArgsException (String usage, String requiredArgs, int receivedArgs) {
            super(usage, "Received "+receivedArgs+" argument(s) but required "+requiredArgs+".");
        }
    }
    
    public static void checkNumArgs (String usage, int minArgs, int maxArgs, int receivedArgs) throws IncorrectNumArgsException {
        if (receivedArgs < minArgs || receivedArgs > maxArgs)
            throw new IncorrectNumArgsException(usage, minArgs, maxArgs, receivedArgs);
    }
    
    public static void checkNumArgs (String usage, int requiredArgs, int receivedArgs) throws IncorrectNumArgsException {
        if (receivedArgs != requiredArgs)
            throw new IncorrectNumArgsException(usage, requiredArgs, receivedArgs);
    }
    
    public static void expectedOneOf (
            String usage,
            String argName,
            String argReceived,
            String ...argOptions)
            throws BadArgumentsException {
        
        // Look through argument options to see if any match
        for (String argOption : argOptions)
            if (argOption.equals(argReceived)) return;
        
        // The method hasn't returned yet, so not argument options matched
        throw new BadArgumentsException(usage,
            "'"+argName+"' argument must be one of: " + String.join(", ", argOptions) + " but '"+argReceived+"' was received.");
    }
    
}
