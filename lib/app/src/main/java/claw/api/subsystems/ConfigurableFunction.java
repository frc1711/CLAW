package claw.api.subsystems;

import edu.wpi.first.util.sendable.SendableBuilder;

public interface ConfigurableFunction {
    public abstract void addToSendable (String prefix, SendableBuilder builder);
}
