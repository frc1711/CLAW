package claw.internal.rct.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import claw.internal.rct.commands.CommandLineInterpreter.CommandLineException;

/**
 * A command obtained through user input which can processed by a {@link CommandLineInterpreter}.
 */
public class Command {
    
    private final String command;
    private final String[] args;
    private final Map<String, String> options = new HashMap<String, String>();
    private final Set<Character> flags = new HashSet<Character>();
    
    /**
     * Gets the command name. For the command line {@code "watch -l swerve --redirect=log.txt"}, this would return
     * {@code "watch"}.
     */
    public String getCommand () {
        return command;
    }
    
    /**
     * Gets the number of arguments provided. For the command line {@code "watch -l swerve --redirect=log.txt"}, this
     * would return {@code 1} because {@code "swerve"} is the only argument.
     */
    public int argsLen () {
        return args.length;
    }
    
    /**
     * Gets the argument string for a given index. For the command line {@code "watch -l swerve --redirect=log.txt"},
     * {@code getArg(0)} would return {@code "swerve"}, because neither {@code "watch"} nor {@code "-l"} are arguments.
     */
    public String getArg (int i) {
        return args[i];
    }
    
    /**
     * Returns whether or not a given character flag is set. Note that flags are required to be letters.
     */
    public boolean isFlagSet (char flag) {
        return flags.contains(flag);
    }
    
    /**
     * Returns whether or not any flags are set.
     */
    public boolean hasAnyFlags () {
        return flags.size() > 0;
    }
    
    /**
     * Returns whether or not a given option is set. If the given option is provided, either as a key-value pair
     * or as a marker, this method will return true.
     */
    public boolean isOptionSet (String option) {
        return options.containsKey(option);
    }
    
    /**
     * Gets the value of a key-value pair option. This method will return {@code null} if the option is given
     * as a marker rather than a key-value pair or if the option was not given at all.<br>
     * <br>
     * So, for the command line
     * {@code "watch --redirect=log.txt --listvals"}, {@code getOptionValue("redirect")} would return
     * {@code "log.txt"} whereas {@code getOptionValue("listvals")} would return {@code null}.
     */
    public String getOptionValue (String option) {
        return options.get(option);
    }
    
    /**
     * Returns whether or not any options are set.
     */
    public boolean hasAnyOptions () {
        return options.size() > 0;
    }
    
    /**
     * Attempt to parse a command line string into a Command object. This constructor will
     * throw an exception if the command is not properly formatted. Read the javadocs for
     * {@link ParseException} to read the rules for command line formatting.
     */
    public Command (String commandText) throws ParseException {
        // Trims leading and trailing whitespace, and ensures the command is not empty
        commandText = commandText.strip();
        if (commandText.equals("")) throw new ParseException("No command given");
        
        // Finalizes the command object if only a command is supplied
        int firstSpaceIndex = commandText.indexOf(' ');
        if (firstSpaceIndex == -1) {
            // Throw an error if the command is not alphanumeric
            if (!isStringAlphanumeric(commandText)) throw new ParseException("Command is not alphanumeric");
            command = commandText;
            args = new String[0];
            return;
        }
        
        // If there is a space present, get the command and then further processing can be done
        command = commandText.substring(0, firstSpaceIndex);
        String[] words = commandText.substring(firstSpaceIndex + 1).split(" ");
        
        List<String> argsList = new ArrayList<String>();
        
        // Loop through words and determine which are arguments, options, and flags
        for (String word : words) {
            // Ensure each word has no trailing or leading whitespace
            word = word.trim();
            
            // If the word is empty, simply move on to the next word
            if (word.equals("")) continue;
            
            if (word.startsWith("--")) {
                // If the word starts with "--", it must be an option
                addOptionsString(options, word.substring(2));
            } else if (word.startsWith("-")) {
                // If the word starts with "-", it must be a flag
                addFlagsString(flags, word.substring(1));
            } else {
                // If the word doesn't start with hyphens, it will be treated as an argument
                argsList.add(word);
            }
        }
        
        // Set the arguments array to match argsList
        args = argsList.toArray(new String[0]);
    }
    
    /**
     * Attempts to parse a string representing an option given as a command, feeding output into a map.
     * Options acting as markers (like "optionname") will be mapped to null. Options acting as key-value
     * pairs (like "optionkey=value") will be mapped to the provided value string. 
     */
    private static void addOptionsString (Map<String, String> options, String optionsString) throws ParseException {
        // Check whether the option is setting a value or simply acting as a flag
        int equalsCharIndex = optionsString.indexOf('=');
        
        if (equalsCharIndex == -1) {
            
            // Option is acting as a marker
            
            // Ensure the option is alphanumeric
            if (!isStringAlphanumeric(optionsString))
                throw new ParseException("Option name is not alphanumeric");
            
            // Ensure the option is not ""
            if (optionsString.equals(""))
                throw new ParseException("Option name is empty");
            
            // Add the option
            options.put(optionsString, null);
        } else {
            
            // Option is acting as a key-value pair
            
            // Get the key-value pair
            String optionName = optionsString.substring(0, equalsCharIndex);
            String optionValue = optionsString.substring(equalsCharIndex + 1);
            
            // Ensure the option is alphanumeric
            if (!isStringAlphanumeric(optionName))
                throw new ParseException("Option name is not alphanumeric");
            
            // Ensure the option is not "" (and neither is its value)
            if (optionName.equals(""))
                throw new ParseException("Option name is empty");
            
            if (optionValue.equals(""))
                throw new ParseException("Option value is empty");
            
            // Set the option key-value pair
            options.put(optionName, optionValue);
        }
    }
    
    private static boolean isStringAlphanumeric (String str) {
        return str.matches("[a-zA-Z0-9]+");
    }
    
    /**
     * Attempts to parse a string representing a set of flags (like "aAf"), feeding output
     * into a provided set of character flags.
     */
    private static void addFlagsString (Set<Character> flags, String flagsString) throws ParseException {
        // No flags set
        if (flagsString.length() == 0) throw new ParseException("List of zero flags set");
        
        // Loop through all character flags
        for (int i = 0; i < flagsString.length(); i ++) {
            // Get the flag character and ensure it is a letter
            final char flag = flagsString.charAt(i);
            if (!Character.isLetter(flag)) throw new ParseException("Found a non-letter flag");
            
            // Add the flag character to the set
            flags.add(flag);
        }
    }
    
    /**
     * Represents an exception that occurred due to improper command line formatting.
     * Below are the rules for command line formatting:
     * <ul>
     * <li>
     *     The general structure is {@code [cmd] [word] [word] [word]}, where words
     *     can be options, flags, or arguments. All commands and words should be separated
     *     by spaces.
     * </li>
     * <li>
     *     The command name must be alphanumeric, and arguments cannot start with a hyphen.
     * </li>
     * <li>
     *    Flag words should follow the format {@code -abc}, where {@code a}, {@code b}, and {@code c}
     *    are enabled flags. Flags are all single characters and must be letters. Flags are case sensitive.
     * </li>
     * <li>
     *    Option words can be one of two types: markers or key-value pairs.
     *    <ul>
     *        <li>
     *            Option markers are very
     *            similar to flags, in that they can be provided to enabled the marker, or not provided to (implicitly)
     *            disable the marker. The syntax for an option marker is {@code --optionname}. The option name
     *            must be alphanumeric.
     *        </li>
     *        <li>
     *            An option can also be a key-value pair, following the syntax {@code --optionname=optionvalue}.
     *            The option name must be alphanumeric, but the option value has no special restrictions, other
     *            than the fact that it cannot be empty (for example, {@code --optionname=} would not be allowed
     *            on its own).
     *        </li>
     *    </ul>
     * </li>
     * </ul>
     */
    public static class ParseException extends CommandLineException {
        public ParseException (String message) {
            super("Malformed command: " + message);
        }
    }
    
}
