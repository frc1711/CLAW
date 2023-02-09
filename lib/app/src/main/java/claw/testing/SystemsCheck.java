package claw.testing;

import claw.rct.network.low.ConsoleManager;

public interface SystemsCheck {
    /**
     * Run the systems check.
     * @param console
     * @return Whether or not the systems check was successful.
     */
    public boolean run (ConsoleManager console);
}
