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

    private final String DEVICE_NAME = "LSCBLU";

    private BluetoothAdapter adapter;

    private boolean isPaired;

    private Integer state;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            }
        }
    };

    private boolean isOff() {
        return state.equals(BluetoothAdapter.STATE_OFF);
    }

    private boolean isTurningOff() {
        return state.equals(BluetoothAdapter.STATE_TURNING_OFF);
    }

    private boolean isOn() {
        return state.equals(BluetoothAdapter.STATE_ON);
    }

    private boolean isTurningOn() {
        return state.equals(BluetoothAdapter.STATE_TURNING_ON);
    }

    @Override
    public boolean isReady() {
        return isEnabled() && isPaired;
    }

    @Override
    public BroadcastReceiver getBroadcastReceiver() {
        return broadcastReceiver;
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void connect() {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
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
