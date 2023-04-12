package claw.replay;

import java.util.HashMap;

import claw.Setting;

public abstract class RobotActionRecorder {
    
    private static final Setting<HashMap<String, RobotActionRecord>>
        REPLAYABLE_RECORDINGS = new Setting<>("claw.replayable_recordings", HashMap::new);
    
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
        REPLAYABLE_RECORDINGS.get().put(name, getRecordingState());
        REPLAYABLE_RECORDINGS.save();
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
