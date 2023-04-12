package claw.rct.console;

import java.util.Optional;

import claw.actions.compositions.Context.TerminatedContextException;

public class ConsoleUtils {
    
    public static double getDoubleValue (ConsoleManager console, String prompt, double minValue, double maxValue) throws TerminatedContextException {
        Optional<Double> answerValue = Optional.empty();
        console.println("");
        while (answerValue.isEmpty()) {
            String answer = getPromptAnswer(console, prompt, true);
            try {
                double val = Double.parseDouble(answer);
                if (val >= minValue && val <= maxValue) {
                    answerValue = Optional.of(val);
                }
            } catch (NumberFormatException e) { }
        }
        
        return answerValue.get();
    }
    
    public static void pressKeyToContinue (ConsoleManager console, String prompt) throws TerminatedContextException {
        console.print("\n"+prompt);
        while (!console.hasInputReady()) { }
        console.clearWaitingInputLines();
        console.clearLine();
    }
    
    public static void pressKeyToContinue (ConsoleManager console) throws TerminatedContextException {
        pressKeyToContinue(console, "(Press any key to continue)");
    }
    
    public static boolean getYesNo (ConsoleManager console, String prompt) throws TerminatedContextException {
        int answer = getStringAnswer(console, prompt, false, "yes", "no");
        return answer == 0;
    }
    
    public static int getStringAnswer (ConsoleManager console, String prompt, boolean caseSensitive, String... possibleAnswers) throws TerminatedContextException {
        int match = -1;
        console.println("");
        while (match == -1) {
            match = getStringMatch(
                getPromptAnswer(console, prompt, true),
                caseSensitive,
                possibleAnswers
            );
        }
        
        return match;
    }
    
    private static String getPromptAnswer (ConsoleManager console, String prompt, boolean clearAboveLine) throws TerminatedContextException {
        if (clearAboveLine) {
            console.moveUp(1);
            console.clearLine();
        }
        
        console.print(prompt);
        return console.readInputLine().strip();
    }
    
    private static int getStringMatch (String input, boolean caseSensitive, String... possibleAnswers) {
        if (!caseSensitive) input = input.toLowerCase();
        for (int i = 0; i < possibleAnswers.length; i ++) {
            if (input.equals(caseSensitive ? possibleAnswers[i] : possibleAnswers[i].toLowerCase())) {
                return i;
            }
        }
        
        return -1;
    }
    
    private ConsoleUtils () { }
    
}
