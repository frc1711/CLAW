package claw.testing;

import claw.rct.commands.CommandLineInterpreter;
import claw.rct.commands.CommandProcessor;
import claw.rct.network.low.ConsoleManager;

public class RobotSystemsChecks {
    
    public final SystemsCheck[] checks;
    
    public void runAllChecks (ConsoleManager console) {
        for (int i = 0; i < checks.length; i ++) {
            console.println("");
            console.println("Check "+(i+1)+" of "+checks.length);
            checks[i].run(console);
            console.println("");
        }
    }
    
    public RobotSystemsChecks (SystemsCheck... checks) {
        this.checks = checks;
    }
    
    public void bindToInterpreter (CommandLineInterpreter interpreter, String commandName, String description) {
        interpreter.addCommandProcessor(new CommandProcessor(
            commandName,
            commandName,
            description,
            (console, a) -> this.runAllChecks(console)
        ));
    }
    
    public void bindToInterpreter (CommandLineInterpreter interpreter, String commandName) {
        bindToInterpreter(
            interpreter,
            commandName,
            "Use this command to run a set of systems checks which can be used to ensure the robot is functioning properly before a match."
        );
    }
    
}
