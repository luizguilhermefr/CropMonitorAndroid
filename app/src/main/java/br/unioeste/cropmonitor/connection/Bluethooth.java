package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;

import br.unioeste.cropmonitor.connection.exceptions.DeviceDoesntSupportBluethoothException;

public class Bluethooth {

    public final static int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter adapter;

    public Bluethooth init() throws DeviceDoesntSupportBluethoothException {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new DeviceDoesntSupportBluethoothException();
        }

        return this;
    }

    public boolean isEnabled() {
        return adapter.isEnabled();
    }

}
