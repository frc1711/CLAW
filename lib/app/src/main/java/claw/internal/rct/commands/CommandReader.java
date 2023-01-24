package claw.internal.rct.commands;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import claw.internal.rct.commands.CommandProcessor.BadCallException;

// TODO: Allowing option markers vs. allowing key-value options
public class CommandReader {
    
    private final Command cmd;
    
    private int nextArgIndex = 0;
    
    public CommandReader (Command cmd) {
        this.cmd = cmd;
    }
    
    public Command getCommand () {
        return cmd;
    }
    
    // Reading arguments
    
    public boolean hasNextArg () {
        return numArgsRemaining() > 0;
    }
    
    public int numArgsRemaining () {
        return cmd.argsLen() - nextArgIndex;
    }
    
    public String readArgString (String argName) throws BadCallException {
        // If the argument was not given then throw an exception
        if (!hasNextArg())
            throw new BadCallException("Expected another argument: \""+argName+"\".");
        
        // Get the argument string and update nextArgIndex
        String arg = cmd.getArg(nextArgIndex);
        nextArgIndex ++;
        return arg;
    }
    
    public int readArgInt (String argName) throws BadCallException {
        return expectInt(readArgString(argName), "\""+argName+"\" argument should have been an integer.");
    }
    
    public double readArgDouble (String argName) throws BadCallException {
        return expectDouble(readArgString(argName), "\""+argName+"\" argument should have been a decimal number.");
    }
    
    public String readArgOneOf (String argName, String errorMessage, Collection<String> argOptions) throws BadCallException {
        return expectOneOf(readArgString(argName), errorMessage, argOptions);
    }
    
    public String readArgOneOf (String argName, String errorMessage, String... argOptions) throws BadCallException {
        return expectOneOf(readArgString(argName), errorMessage, argOptions);
    }
    
    public void noMoreArgs () throws BadCallException {
        if (hasNextArg())
            throw new BadCallException("Expected "+numArgsRemaining()+" fewer arguments.");
    }
    
    public void allowNoArgs () throws BadCallException {
        if (cmd.argsLen() > 0)
            throw new BadCallException("Expected no arguments.");
    }
    
    // Flags
    
    public boolean getFlag (char flag) {
        return cmd.isFlagSet(flag);
    }
    
    public void allowNoFlags () throws BadCallException {
        allowFlags(new char[0]);
    }
    
    public void allowFlags (char... allowedFlags) throws BadCallException {
        String allowedFlagsString = "";
        for (char c : allowedFlags)
            allowedFlagsString += c;
        allowFlags(allowedFlagsString);
    }
    
    public void allowFlags (String allowedFlags) throws BadCallException {
        char[] flagsGiven = cmd.getAllFlags();
        for (char flag : flagsGiven)
            if (allowedFlags.indexOf(flag, 0) == -1)
                throw new BadCallException("Flag '"+flag+"' is not allowed.");
    }
    
    // Options
    
    public boolean getOptionMarker (String optionName) {
        return cmd.isOptionSet(optionName) && cmd.getOptionValue(optionName) == null;
    }
    
    public boolean hasKeyValueOption (String optionName) {
        return cmd.isOptionSet(optionName) && cmd.getOptionValue(optionName) != null;
    }
    
    public String readOptionValueString (String optionName) throws BadCallException {
        if (!hasKeyValueOption(optionName))
            throw new BadCallException("Expected the missing option value for \""+optionName+"\"");
        return cmd.getOptionValue(optionName);
    }
    
    public int readOptionValueInt (String optionName) throws BadCallException {
        return expectInt(readOptionValueString(optionName), "Option '"+optionName+"' should have been an integer.");
    }
    
    public double readOptionValueDouble (String optionName) throws BadCallException {
        return expectInt(readOptionValueString(optionName), "Option '"+optionName+"' should have been a decimal number.");
    }
    
    public String readOptionValueOneOf (String optionName, String errorMessage, Collection<String> options) throws BadCallException {
        return expectOneOf(readOptionValueString(optionName), errorMessage, options);
    }
    
    public String readOptionValueOneOf (String optionName, String errorMessage, String... options) throws BadCallException {
        return expectOneOf(readOptionValueString(optionName), errorMessage, options);
    }
    
    public void allowNoOptions () throws BadCallException {
        allowOptions(new String[0]);
    }
    
    public void allowOptions (String... allowedOptions) throws BadCallException {
        allowOptions(List.of(allowedOptions));
    }
    
    public void allowOptions (Collection<String> allowedOptions) throws BadCallException {
        String[] optionsGiven = cmd.getAllOptions();
        
        for (String option : optionsGiven)
            if (!allowedOptions.contains(option))
                throw new BadCallException("Option '"+option+"' is not allowed.");
    }
    
    // Misc.
    
    public void allowNone () throws BadCallException {
        allowNoFlags();
        allowNoOptions();
        allowNoArgs();
    }
    
    // Utility functions mostly used internally
    
    private int expectInt (String string, String errorMessage) throws BadCallException {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            throw new BadCallException(errorMessage);
        }
    }
    
    private double expectDouble (String string, String errorMessage) throws BadCallException {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            throw new BadCallException(errorMessage);
        }
    }
    
    private String expectOneOf (String string, String errorMessage, Collection<String> options) throws BadCallException {
        // Look through options to see if any match the string
        Iterator<String> iter = options.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(string))
                return string;
        }
        
        throw new BadCallException(errorMessage);
    }
    
    private String expectOneOf (String string, String errorMessage, String[] options) throws BadCallException {
        return expectOneOf(string, errorMessage, List.of(options));
    }
    
}
