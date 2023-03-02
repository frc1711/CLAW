package claw.rct.network.low.concurrency;

import java.time.Instant;

public class KeepaliveWatcher {
    
    private final long
        keepaliveDurationMillis,
        keepaliveSendIntervalMillis;
    private final Runnable
        terminate,
        sendKeepaliveMessage;
    private long keepaliveUntil = 0;
    private boolean terminated = false;
    private Thread watcherThread;
    
    public KeepaliveWatcher (long keepaliveDurationMillis, long keepaliveSendIntervalMillis, Runnable sendKeepaliveMessage, Runnable terminate) {
        this.keepaliveDurationMillis = keepaliveDurationMillis;
        this.keepaliveSendIntervalMillis = keepaliveSendIntervalMillis;
        this.sendKeepaliveMessage = sendKeepaliveMessage;
        this.terminate = terminate;
    }
    
    public void start () {
        if (terminated) return;
        continueKeepalive();
        watcherThread = getWatcherThread();
        watcherThread.start();
    }
    
    private Thread getWatcherThread () {
        return new Thread(() -> {
            while (!terminated) {
                if (!shouldStayAlive()) {
                    terminated = true;
                    terminate.run();
                } else {
                    sendKeepaliveMessage.run();
                }
                
                try {
                    Thread.sleep(keepaliveSendIntervalMillis);
                } catch (InterruptedException e) { }
            }
        });
    }
    
    public void continueKeepalive () {
        if (!terminated)
            keepaliveUntil = getCurrentTimeMillis() + keepaliveDurationMillis;
    }
    
    public boolean shouldStayAlive () {
        return !terminated && getCurrentTimeMillis() <= keepaliveUntil;
    }
    
    public long getCurrentTimeMillis () {
        return Instant.now().toEpochMilli();
    }
    
    public void stopWatching () {
        terminated = true;
        if (watcherThread != null)
            watcherThread.interrupt();
    }
    
}
