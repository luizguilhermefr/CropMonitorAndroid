package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.util.Set;

import br.unioeste.cropmonitor.connection.contracts.ConnectionInterface;

public class Bluethooth implements ConnectionInterface {

    public final String DEVICE_NAME = "LSCBLU";

    private BluetoothAdapter adapter;

    Set<BluetoothDevice> pairedDevices;

    private boolean isPaired;

    @Override
    public boolean isReady() {
        return isEnabled() && isPaired;
    }

    @Override
    public void connect() {
        pairedDevices = adapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(DEVICE_NAME)) {
                isPaired = true;
                return;
            }
        }
        isPaired = false;
    }

    @Override
    public void init() throws IOException {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IOException("Device doesn't support Bluethooth connection.");
        }
    }

    @Override
    public Intent requestEnablement() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    @Override
    public boolean isEnabled() {
        return adapter.isEnabled();
    }

}
