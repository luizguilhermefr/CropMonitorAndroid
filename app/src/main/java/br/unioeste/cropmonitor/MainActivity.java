package br.unioeste.cropmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;

import br.unioeste.cropmonitor.connection.BluetoothConnection;
import br.unioeste.cropmonitor.ui.Sensor;
import br.unioeste.cropmonitor.util.Protocol;
import br.unioeste.cropmonitor.util.exceptions.ProtocolException;

public class MainActivity extends AppCompatActivity {

    private static final int SENSOR_1 = 0;
    private static final int SENSOR_2 = 1;
    private static final int SENSOR_3 = 2;
    private static final int SENSOR_4 = 3;
    private BluetoothConnection bluetoothConnection;
    private BroadcastReceiver broadcastActionState;
    private BroadcastReceiver broadcastBondState;
    private BroadcastReceiver broadcastConnectionStatus;
    private BroadcastReceiver broadcastSensorUpdate;

    private LinearLayout rootLinearLayout;

    private Sensor sensor1;
    private Sensor sensor2;
    private Sensor sensor3;
    private Sensor sensor4;

    private Handler uiHandler = new Handler();
    private ProgressBar progressBar;
    private Integer sensorUpdating = -1;

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
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastConnectionStatus, BluetoothConnection.getIntentFilterForConnect());
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastSensorUpdate, BluetoothConnection.getIntentFilterForUpdate());
    }

    private void unregisterBroadcasters() {
        try {
            unregisterReceiver(broadcastActionState);
            unregisterReceiver(broadcastBondState);
            unregisterReceiver(broadcastConnectionStatus);
            unregisterReceiver(broadcastSensorUpdate);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
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

    private void restartBluetoothConnection() {
        BluetoothDevice bondedDevice = bluetoothConnection.getBondedDevice();
        if (bondedDevice != null) {
            onDeviceBonded(bondedDevice);
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
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void onFailureConnection() {
        generateToast(R.string.connection_error);
        bluetoothConnection.disconnect();
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void onAttemptingConnection() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSuccessfulConnection() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void onDeviceRespondedWithError() {
        generateToast(R.string.sensor_responded_with_error + " " + sensorUpdating);
    }

    private void onMessageArrived(String message) {
        try {
            Protocol protocolTranscoder = new Protocol(message);
            if (protocolTranscoder.ok()) {
                updateSensorUi(sensorUpdating, protocolTranscoder.getValue());
            } else {
                onDeviceRespondedWithError();
            }
        } catch (ProtocolException e) {
            // onProtocolError
            System.out.println("PROTOCOL EXCEPTION --------------------> " + message);
            System.out.flush();
        }
    }

    private void updateSensorUi(final Integer sensor, final BigDecimal value) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                switch (sensor) {
                    case SENSOR_1:
                        sensor1.setValue(value);
                        break;
                    case SENSOR_2:
                        sensor2.setValue(value);
                        break;
                    case SENSOR_3:
                        sensor3.setValue(value);
                        break;
                    case SENSOR_4:
                        sensor4.setValue(value);
                        break;
                }
            }
        });
    }

    private void requestSensorUpdate(final Integer sensor) {
        if (bluetoothConnection != null && bluetoothConnection.connected()) {
            sensorUpdating = sensor;
            String read = Protocol.makeReadString(sensor);
            bluetoothConnection.write(read.getBytes());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sensor1 = new Sensor(MainActivity.this, SENSOR_1, getResources().getString(R.string.sensor1_title));
        sensor2 = new Sensor(MainActivity.this, SENSOR_2, getResources().getString(R.string.sensor2_title));
        sensor3 = new Sensor(MainActivity.this, SENSOR_3, getResources().getString(R.string.sensor3_title));
        sensor4 = new Sensor(MainActivity.this, SENSOR_4, getResources().getString(R.string.sensor4_title));

        rootLinearLayout = findViewById(R.id.root);
        rootLinearLayout.addView(sensor1.getElements());
        rootLinearLayout.addView(sensor2.getElements());
        rootLinearLayout.addView(sensor3.getElements());
        rootLinearLayout.addView(sensor4.getElements());

        progressBar = findViewById(R.id.activityIndicator);
        progressBar.setVisibility(View.INVISIBLE);

        broadcastActionState = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_ON:
                            restartBluetoothConnection();
                            break;
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
                if (action != null && action.equals(BluetoothConnection.CONNECTION)) {
                    final Integer content = intent.getIntExtra(BluetoothConnection.STATUS, Integer.MAX_VALUE);
                    switch (content) {
                        case BluetoothConnection.STATUS_FAILURE:
                            onFailureConnection();
                            break;
                        case BluetoothConnection.STATUS_SOCKET_FAILURE:
                            onFailureConnection();
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

        broadcastSensorUpdate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null && action.equals(BluetoothConnection.UPDATE)) {
                    final String content = intent.getStringExtra(BluetoothConnection.SENSOR);
                    onMessageArrived(content);
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
