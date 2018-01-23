package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.util.Set;

public class BluetoothConnection {

    private final String DEVICE_NAME = "LSCBLU";

    private BluetoothAdapter adapter;

    private boolean isPaired;

    public boolean isPaired() {
        return isPaired;
    }

    public IntentFilter getIntentFilterForActionState() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public void attemptPair() {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(DEVICE_NAME)) {
                isPaired = true;
                return;
            }
        }
        isPaired = false;
    }

    public void initializeAdapter() throws IOException {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IOException("Device doesn't support BluetoothConnection connection.");
        }
    }

    public Intent getIntentForEnabling() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    public boolean isEnabled() {
        return adapter.isEnabled();
    }
}
