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
     * @return                          Whether or not the command was recognized by this interpreter. Use
     * {@link #addCommandConsumer(String, Consumer)} to add more recognized commands.
     * @throws Command.ParseException   An exception thrown if the command is malformed
     */
    public boolean processLine (String line) throws Command.ParseException {
        Command commandObj = new Command(line);
        String commandName = commandObj.getCommand().toUpperCase();
        
        // Send the command to its consumer if the command name exists in the commandConsumers hashmap,
        // otherwise throws an exception
        if (commandConsumers.containsKey(commandName)) {
            commandConsumers.get(commandName).accept(commandObj);
            return true;
        } else return false;
    }
    
}
