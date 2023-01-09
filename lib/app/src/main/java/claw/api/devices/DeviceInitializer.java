package claw.api.devices;

public interface DeviceInitializer <T> {
    public T initializeDevice (int deviceId);
}
