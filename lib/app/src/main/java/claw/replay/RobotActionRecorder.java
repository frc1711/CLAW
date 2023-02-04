package claw.replay;

import java.util.ArrayList;
import java.util.HashMap;

import claw.SettingsManager;
import claw.SettingsManager.Setting;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

/**
 * A thread-safe class used internally by CLAW to record {@link RobotAction}s for autonomous replay.
 */
public class RobotActionRecorder {
    
    private static final Setting<HashMap<String, ActionsReplaySerial>> REPLAYABLE_COMMANDS = SettingsManager.getSetting("REPLAYABLE_COMMANDS");
    private static final ArrayList<RobotAction> actionsList = new ArrayList<RobotAction>();
    
    private static boolean recordingEnabled = false;
    
    /**
     * Adds a {@link RobotAction} to the internal actions buffer. This does nothing if action recording is not enabled.
     * @param action
     */
    public static void addAction (RobotAction action) {
        if (!recordingEnabled) return;
        
        synchronized (actionsList) {
            actionsList.add(action);
        }
    }
    
    /**
     * Clears the internal actions buffer.
     */
    public static void resetActionsBuffer () {
        synchronized (actionsList) {
            actionsList.clear();
        }
    }
    
    /**
     * Sets whether or not actions should be recorded. If recording is disabled, then actions added via {@link #addAction(RobotAction)}
     * will be ignored.
     * @param enabled
     */
    public static void setRecordingEnabled (boolean enabled) {
        recordingEnabled = enabled;
    }
    
    /**
     * Add all the {@link RobotAction}s in the actions buffer to a new {@link ActionsReplaySerial} with a given name
     * and description. This method will also clear the actions buffer.
     * @param name
     * @param description
     * @return
     */
    public static ActionsReplaySerial getReplaySerial (String name, String description) {
        RobotAction[] actionsArray;
        synchronized (actionsList) {
            actionsArray = new RobotAction[actionsList.size()];
            actionsList.toArray(actionsArray);
        }
        
        resetActionsBuffer();
        
        return new ActionsReplaySerial(name, description, actionsArray);
    }
    
    /**
     * Saves a given {@link ActionsReplaySerial} to settings so that it can be replayed later.
     * This will overwrite an existing saved {@code ActionsReplaySerial} if it has the same name.
     * @param serial
     */
    public static void saveReplaySerial (ActionsReplaySerial serial) {
        REPLAYABLE_COMMANDS.getValue(new HashMap<>()).put(serial.name, serial);
        SettingsManager.save(); // TODO: Saving individual fields rather than the whole settings configuration
    }
    
    /**
     * Represents a list of {@link RobotAction}s to be completed sequentially. This class is used
     * for saving actions to the roboRIO so they can be used later as autonomous commands.
     */
    public static record ActionsReplaySerial (String name, String description, RobotAction[] actions) {
        
        /**
         * Convert this {@link ActionsReplaySerial} into a command which can be executed autonomously.
         * The command simply executes each of its actions sequentially.
         * @return The autonomous replay {@link Command}.
         */
        public Command toReplayCommand () {
            // Create an array containing all the commands to be executed
            Command[] commands = new Command[actions.length];
            for (int i = 0; i < commands.length; i ++)
                commands[i] = actions[i].toReplayCommand();
            
            // Turn the array of commands into one command which executes them sequentially
            return new SequentialCommandGroup(commands);
        }
        
    }
    
}
