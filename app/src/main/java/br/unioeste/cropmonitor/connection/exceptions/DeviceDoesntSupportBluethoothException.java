package br.unioeste.cropmonitor.connection.exceptions;

public class DeviceDoesntSupportBluethoothException extends Exception {
    public DeviceDoesntSupportBluethoothException(String message) {
        super(message);
    }

    public DeviceDoesntSupportBluethoothException() {
        super();
    }
}
