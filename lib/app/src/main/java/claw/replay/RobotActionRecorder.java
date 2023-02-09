package claw.replay;

import java.util.HashMap;

import claw.SettingsManager;
import claw.SettingsManager.Setting;

public abstract class RobotActionRecorder {
    
    private static final Setting<HashMap<String, RobotActionRecord>> REPLAYABLE_RECORDINGS = SettingsManager.getSetting("REPLAYABLE_RECORDINGS");
    
    private boolean recordingEnabled = false;
    
    protected abstract void resetRecordingState ();
    protected abstract RobotActionRecord getRecordingState ();
    
    public void stopRecording () {
        recordingEnabled = false;
    }
    
    public void startRecording () {
        resetRecordingState();
        recordingEnabled = true;
    }
    
    public void saveRecordingAs (String name) {
        stopRecording();
        REPLAYABLE_RECORDINGS.getValue(new HashMap<>()).put(name, getRecordingState());
        SettingsManager.save(); // TODO: Saving individual fields rather than the whole settings configuration
    }
    
    /**
     * Check whether or not recording is currently enabled. If recording is disabled, then the internal recording state should
     * not be updated.
     * @return
     */
    public boolean isRecording () {
        return recordingEnabled;
    }
    
}
