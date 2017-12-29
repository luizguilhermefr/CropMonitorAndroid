package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;

import br.unioeste.cropmonitor.connection.contracts.ConnectionInterface;

public class Bluethooth implements ConnectionInterface {

    private BluetoothAdapter adapter;

    private int status = BluetoothAdapter.STATE_OFF;

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
            }
        }
    };

    public BroadcastReceiver getStatusReceiver() {
        return statusReceiver;
    }

    public IntentFilter getBroadcastFilter() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public void init() throws IOException {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IOException("Device doesn't support Bluethooth connection.");
        }
    }

    public Intent requestEnablement () {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    public boolean isEnabled() {
        return adapter.isEnabled();
    }

}
