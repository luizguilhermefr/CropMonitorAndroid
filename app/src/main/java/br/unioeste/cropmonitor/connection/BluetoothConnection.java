package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.util.Set;

public class BluetoothConnection {

    private final String DEVICE_NAME = "LSCBLU";
    private final BroadcastReceiver broadcastActionState;
    private BluetoothAdapter adapter;
    private boolean isPaired;
    private Integer state;

    public BluetoothConnection() {
        broadcastActionState = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                }
            }
        };
    }

    public boolean isOff() {
        return state.equals(BluetoothAdapter.STATE_OFF);
    }

    public boolean isTurningOff() {
        return state.equals(BluetoothAdapter.STATE_TURNING_OFF);
    }

    public boolean isOn() {
        return state.equals(BluetoothAdapter.STATE_ON);
    }

    public boolean isTurningOn() {
        return state.equals(BluetoothAdapter.STATE_TURNING_ON);
    }

    public boolean isPaired() {
        return isPaired;
    }

    public BroadcastReceiver getBroadcastReceiverForActionState() {
        return broadcastActionState;
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
