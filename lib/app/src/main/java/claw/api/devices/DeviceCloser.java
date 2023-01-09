package claw.api.devices;

public interface DeviceCloser <T> {
    public void closeDevice (T device) throws Exception;
}
