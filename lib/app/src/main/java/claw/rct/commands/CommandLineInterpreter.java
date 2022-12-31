package claw.rct.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import claw.rct.commands.CommandProcessor.BadArgumentsException;
import claw.rct.commands.CommandProcessor.HelpMessage;
import claw.rct.network.low.ConsoleManager;

/**
 * Represents a system for interpreting {@link Command}s. Recognized commands can be added via
 * {@link #addCommandProcessor(String, CommandProcessor)} and commands can be parsed and processed
 * via {@link #processLine(String)}.
 */
public class CommandLineInterpreter {
    
    private final HashMap<String, CommandProcessor> commandProcessors = new HashMap<String, CommandProcessor>();
    
    /**
     * Recognizes a new command name as valid. The command name is case insensitive. Note that any command processors
     * added to this interpreter with the same command name are silently removed as a result of calling this method.
     * @param commandProcessor The {@link CommandProcessor}.
     */
    public void addCommandProcessor (CommandProcessor commandProcessor) {
        commandProcessors.put(commandProcessor.command.toUpperCase(), commandProcessor);
    }
    
    /**
     * Processes a line into a {@link Command} and attempts to call one of this interpreter's command consumers
     * on the command. See {@link Command.ParseException} for command line formatting. Command names
     * are case insensitive.
     * @param console                           The {@link ConsoleManager} to put output to and take input from.
     * @param line                              The command line string.
     * @throws Command.ParseException           An exception thrown if the command is malformed.
     * @throws BadArgumentsException            An exception thrown by the command processor if it received bad arguments.
     * @throws CommandNotRecognizedException    An exception thrown if the command is not recognized by this interpreter.
     */
    public void processLine (ConsoleManager console, String line)
            throws Command.ParseException, CommandNotRecognizedException, BadArgumentsException {
        // Attempt to parse the command and get the command name
        Command commandObj = new Command(line);
        String commandName = commandObj.getCommand().toUpperCase();
        
        // Send the command to its processor if the command name exists in the commandProcessors hashmap,
        // otherwise throws an exception
        if (commandProcessors.containsKey(commandName)) {
            CommandProcessor processor = commandProcessors.get(commandName);
            try {
                
                // Command is recognized, so run it
                processor.function.process(console, commandObj);
                
            } catch (BadArgumentsException exception) {
                // Command threw a BadArgumentsException, so tack on the CommandProcessor and throw it again
                throw exception.forCommandProcessor(processor);
            }
        } else {
            throw new CommandNotRecognizedException(commandObj.getCommand());
        }
    }
    
    /**
     * Gets a list of {@link HelpMessage}s corresponding to each {@link CommandProcessor} recognized
     * by this {@link CommandLineInterpreter}.
     * @return The list of {@code HelpMessage}s, sorted alphabetically by command name.
     */
    public List<HelpMessage> getHelpMessages () {
        // Get a list of help messages from the hashmap containing command processors
        List<HelpMessage> helpMessages = new ArrayList<HelpMessage>();
        commandProcessors.values().forEach(processor -> helpMessages.add(processor.helpMessage));
        
        // Sort the list alphabetically and return it
        helpMessages.sort((a, b) -> a.getCommand().compareTo(b.getCommand()));
        return helpMessages;
    }
    
    /**
     * An exception which can occur as a result of processing a bad command line.
     */
    public static class CommandLineException extends Exception {
        
        private final String errorMessage;
        
        /**
         * Creates a new command line exception.
         * @param errorMessage The error message to print to the console.
         */
        public CommandLineException (String errorMessage) {
            super(errorMessage);
            this.errorMessage = errorMessage;
        }
        
        /**
         * Writes the error message of the {@link CommandLineException} to the console.
         * @param console The {@link ConsoleManager} to write the error message to.
         */
        public void writeToConsole (ConsoleManager console) {
            console.printlnErr(errorMessage);
        }
        
    }
    
    public static class CommandNotRecognizedException extends CommandLineException {
        public CommandNotRecognizedException (String command) {
            super("'" + command + "' is not recognized as a command.");
        }
    }
    
}
