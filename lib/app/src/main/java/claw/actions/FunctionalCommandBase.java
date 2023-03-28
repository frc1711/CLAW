package claw.actions;

import edu.wpi.first.wpilibj2.command.Command;

public interface FunctionalCommandBase <T> extends Command {
    public abstract T getValue ();
}
