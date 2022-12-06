package rct.commands;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Represents a system for interpreting {@link Command}s. Recognized commands can be added via
 * {@link #addCommandConsumer(String, Consumer)} and commands can be parsed and processed
 * via {@link #processLine(String)}.
 */
public class CommandInterpreter {
    
    private final HashMap<String, Consumer<Command>> commandConsumers = new HashMap<String, Consumer<Command>>();
    
    public CommandInterpreter () { }
    
    /**
     * Recognizes a new command name as valid. The command name is case insensitive.
     * @param command   The command's name. See {@link Command.ParseException} for
     * command line formatting.
     * @param receiver  A {@code Consumer<Command>} which can accept a command and process it.
     */
    public void addCommandConsumer (String command, Consumer<Command> receiver) {
        commandConsumers.put(command.toUpperCase(), receiver);
    }
    
    /**
     * Processes a line into a {@link Command} and attempts to call one of this interpreter's command consumers
     * on the command. See {@link Command.ParseException} for command line formatting.
     * @param line                      The command line string
     * @throws Command.ParseException   An exception thrown if the command is malformed
     * @throws UnknownCommandException  An exception thrown if this interpreter is not configured to accept
     * the command given its name. Use {@link #addCommandConsumer(String, Consumer)} to add another recognized
     * command.
     */
    public void processLine (String line) throws Command.ParseException, UnknownCommandException {
        Command commandObj = new Command(line);
        String commandName = commandObj.getCommand().toUpperCase();
        
        // Send the command to its consumer if the command name exists in the commandConsumers hashmap,
        // otherwise throws an exception
        if (commandConsumers.containsKey(commandName))
            commandConsumers.get(commandName).accept(commandObj);
        else
            throw new UnknownCommandException(commandName);
    }
    
    /**
     * An exception that can occur when a parsed command name is not recognized by the interpreter
     */
    public static class UnknownCommandException extends Exception {
        public UnknownCommandException (String commandName) {
            super("Command '"+commandName+"' is not recognized by the command interpreter");
        }
    }
    
}
