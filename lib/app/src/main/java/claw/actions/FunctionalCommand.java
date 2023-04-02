package claw.actions;

import edu.wpi.first.wpilibj2.command.Command;

public interface FunctionalCommand <T> extends Command {
    public abstract T getValue ();
}
