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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

import br.unioeste.cropmonitor.connection.BluetoothConnection;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_ENABLE = 1;

    private BluetoothConnection bluetoothConnection;

    private BroadcastReceiver broadcastActionState;

    private BroadcastReceiver broadcastDiscoverability;

    private BroadcastReceiver broadcastDeviceFound;

    private BroadcastReceiver broadcastBondState;

    private Button btnStart;

    private TextView sensor1Content;

    private TextView sensor2Content;

    private TextView sensor3Content;

    private TextView sensor4Content;

    private Handler uiHandler = new Handler();

    private boolean polling = false;

    private Thread pollThread = null;

    private void generateToast(CharSequence text, int length) {
        Context context = getApplicationContext();
        Toast.makeText(context, text, length).show();
    }

    private void onUnsupportedDevice(String message) {
        System.out.println(message);
        CharSequence text = getResources().getString(R.string.device_not_supported);
        generateToast(text, Toast.LENGTH_LONG);
    }

    private void onErrorConnection(String message) {
        System.out.println(message);
        CharSequence text = getResources().getString(R.string.connection_error);
        generateToast(text, Toast.LENGTH_LONG);
    }

    private void onDeniedConnection(String message) {
        System.out.println(message);
        CharSequence text = getResources().getString(R.string.connection_denied);
        generateToast(text, Toast.LENGTH_LONG);
    }

    private void onConnected() {
        CharSequence text = getResources().getString(R.string.connection_successful);
        generateToast(text, Toast.LENGTH_LONG);
    }

    private void onDisconnecting() {
        CharSequence text = getResources().getString(R.string.disconnecting);
        generateToast(text, Toast.LENGTH_LONG);
    }

    private void requestBluetoothEnablement() {
        Intent enableIntent = bluetoothConnection.getIntentForEnabling();
        startActivityForResult(enableIntent, REQUEST_ENABLE);
    }

    private void pair() {
        bluetoothConnection.attemptPair();
        if (bluetoothConnection.isPaired()) {
            onConnected();
        } else {
            onErrorConnection("Connection is not ready.");
        }
    }

    private void connect() {
        bluetoothConnection = new BluetoothConnection();
        try {
            bluetoothConnection.initializeAdapter();
            if (!bluetoothConnection.isEnabled()) {
                requestBluetoothEnablement();
            } else {
                pair();
            }
        } catch (IOException e) {
            onUnsupportedDevice("Initialization thrown IOException.");
            e.printStackTrace();
        }
    }

    private void disconnect() {
        onDisconnecting();
        polling = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CANCELED) {
            onDeniedConnection("Connection was refused by user.");
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

        btnStart = findViewById(R.id.startPollingBtn);
        btnStart.setEnabled(false);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                polling = true;
                btnStart.setEnabled(false);
                poll();
            }
        });

        broadcastActionState = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            //
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            disconnect();
                            btnStart.setEnabled(false);
                            break;
                        case BluetoothAdapter.STATE_ON:
                            pair();
                            btnStart.setEnabled(bluetoothConnection.isPaired() && !polling);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            //
                            break;
                    }
                }
            }
        };

        broadcastDiscoverability = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                    Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                            //
                            break;
                        case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                            //
                            break;
                        case BluetoothAdapter.SCAN_MODE_NONE:
                            //
                            break;
                        case BluetoothAdapter.STATE_CONNECTING:
                            //
                            break;
                        case BluetoothAdapter.STATE_CONNECTED:
                            //
                            break;
                    }
                }
            }
        };

        broadcastDeviceFound = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // TODO: Something
                }
            }
        };

        broadcastBondState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDED:
                            //
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            //
                            break;
                        case BluetoothDevice.BOND_NONE:
                            //
                            break;
                    }
                }
            }
        };

        registerReceiver(broadcastActionState, bluetoothConnection.getIntentFilterForActionState());
        registerReceiver(broadcastDiscoverability, bluetoothConnection.getIntentFilterForDiscoverability());
        registerReceiver(broadcastDeviceFound, bluetoothConnection.getIntentFilterForDeviceFound());
        registerReceiver(broadcastBondState, bluetoothConnection.getIntentFilterForBondState());
        connect();
    }

    public void poll() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Integer sensor1, sensor2, sensor3, sensor4;
                for (; ; ) {
                    if (polling) {
                        sensor1 = new Random().nextInt();
                        sensor2 = new Random().nextInt();
                        sensor3 = new Random().nextInt();
                        sensor4 = new Random().nextInt();

                        updateSensorsUi(String.valueOf(sensor1), String.valueOf(sensor2), String.valueOf(sensor3), String.valueOf(sensor4));
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        pollThread = new Thread(runnable);
        pollThread.start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastActionState);
        unregisterReceiver(broadcastDiscoverability);
        unregisterReceiver(broadcastDeviceFound);
        unregisterReceiver(broadcastBondState);
    }
}
