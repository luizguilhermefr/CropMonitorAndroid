package br.unioeste.cropmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import br.unioeste.cropmonitor.connection.BluetoothConnection;

public class MainActivity extends AppCompatActivity {

    private BluetoothConnection bluetoothConnection;
    private BroadcastReceiver broadcastActionState;
    private BroadcastReceiver broadcastBondState;
    private BroadcastReceiver broadcastConnectionStatus;
    private TextView sensor1Content;
    private TextView sensor2Content;
    private TextView sensor3Content;
    private TextView sensor4Content;
    private Button sensor1Update;
    private Button sensor2Update;
    private Button sensor3Update;
    private Button sensor4Update;
    private Handler uiHandler = new Handler();
    private ProgressBar progressBar;

    private void generateToast(String text) {
        Context context = getApplicationContext();
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    private void generateToast(Integer resource) {
        CharSequence text = getResources().getString(resource);
        Context context = getApplicationContext();
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    private void registerBroadcasters() {
        registerReceiver(broadcastActionState, BluetoothConnection.getIntentFilterForActionState());
        registerReceiver(broadcastBondState, BluetoothConnection.getIntentFilterForBondState());
    }

    private void unregisterBroadcasters() {
        unregisterReceiver(broadcastActionState);
        unregisterReceiver(broadcastBondState);
    }

    private void startBluetoothConnection() {
        bluetoothConnection = new BluetoothConnection(MainActivity.this);
        registerBroadcasters();
        try {
            bluetoothConnection.checkAdapter();
            if (!bluetoothConnection.isEnabled()) {
                Intent enableIntent = BluetoothConnection.getIntentForEnabling();
                startActivityForResult(enableIntent, 1);
            }
            BluetoothDevice bondedDevice = bluetoothConnection.getBondedDevice();
            if (bondedDevice != null) {
                onDeviceBonded(bondedDevice);
            }
        } catch (IOException e) {
            generateToast(R.string.device_not_supported);
            e.printStackTrace();
        }
    }

    private void onDeviceBonded(BluetoothDevice device) {
        generateToast(getResources().getString(R.string.bonded_with) + " " + device.getName());
        bluetoothConnection.setPairedDevice(device).initialize();
    }

    private void onDeviceBonding(BluetoothDevice device) {
        generateToast(getResources().getString(R.string.bonding_with) + " " + device.getName());
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onDeviceDisconnected() {
        generateToast(R.string.disconnecting);
        bluetoothConnection.disconnect();
        setUpdatersEnabled(false);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void onAttemptingConnection() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSuccessfulConnection() {
        setUpdatersEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void updateSensorsUi(final String sensor1, final String sensor2, final String sensor3, final String sensor4) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sensor1Content.setText(sensor1);
                sensor2Content.setText(sensor2);
                sensor3Content.setText(sensor3);
                sensor4Content.setText(sensor4);
            }
        });
    }

    private void setUpdatersEnabled(final Boolean enabled) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sensor1Update.setEnabled(enabled);
                sensor2Update.setEnabled(enabled);
                sensor3Update.setEnabled(enabled);
                sensor4Update.setEnabled(enabled);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CANCELED) {
            generateToast(R.string.authorization_denied);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sensor1Content = findViewById(R.id.sensor1Content);
        sensor2Content = findViewById(R.id.sensor2Content);
        sensor3Content = findViewById(R.id.sensor3Content);
        sensor4Content = findViewById(R.id.sensor4Content);

        sensor1Update = findViewById(R.id.sensor1Update);
        sensor2Update = findViewById(R.id.sensor2Update);
        sensor3Update = findViewById(R.id.sensor3Update);
        sensor4Update = findViewById(R.id.sensor4Update);

        progressBar = findViewById(R.id.activityIndicator);
        progressBar.setVisibility(View.INVISIBLE);

        broadcastActionState = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            onDeviceDisconnected();
                            break;
                    }
                }
            }
        };

        broadcastBondState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getName().equals(BluetoothConnection.DEVICE_NAME)) {
                        switch (device.getBondState()) {
                            case BluetoothDevice.BOND_BONDED:
                                onDeviceBonded(device);
                                break;
                            case BluetoothDevice.BOND_BONDING:
                                onDeviceBonding(device);
                                break;
                            case BluetoothDevice.BOND_NONE:
                                onDeviceDisconnected();
                                break;
                        }
                    }
                }
            }
        };

        broadcastConnectionStatus = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null && action.equals(BluetoothConnection.CONNECT)) {
                    final Integer content = intent.getIntExtra(BluetoothConnection.STATUS, Integer.MAX_VALUE);
                    switch (content) {
                        case BluetoothConnection.STATUS_FAILURE:
                            //
                            break;
                        case BluetoothConnection.STATUS_WORKING:
                            onAttemptingConnection();
                            break;
                        case BluetoothConnection.STATUS_OK:
                            onSuccessfulConnection();
                            break;
                    }

                }
            }
        };

        startBluetoothConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcasters();
    }
}
