package br.unioeste.cropmonitor;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import br.unioeste.cropmonitor.connection.BluetoothConnection;
import br.unioeste.cropmonitor.ui.Sensor;
import br.unioeste.cropmonitor.util.Protocol;
import br.unioeste.cropmonitor.util.exceptions.ProtocolException;
import me.bendik.simplerangeview.SimpleRangeView;

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
    private ArrayList<Sensor> sensors;
    private ProgressBar progressBar;
    private SimpleRangeView thresholdRangeView;
    private View dialogView;

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

    private void onProtocolException() {
        // Restart connection ?
    }

    private void onAttemptingConnection() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSuccessfulConnection() {
        progressBar.setVisibility(View.INVISIBLE);
        bluetoothConnection.write(Protocol.makeRequestSyncMessage().getBytes());
    }

    private void onDeviceRespondedWithError() {
        generateToast(R.string.sensor_responded_with_error);
    }

    private void onMessageArrived(String message) {
        try {
            Protocol protocolParser = new Protocol(message);
            if (protocolParser.ok()) {
                if (protocolParser.isUpdateSensor()) {
                    updateSensorUi(protocolParser.getSensor(), protocolParser.getValue());
                } else if (protocolParser.isUpdateLowerThreshold()) {
                    updateSensorLowerThreshold(protocolParser.getSensor(), protocolParser.getValue());
                } else if (protocolParser.isUpdateUpperThreshold()) {
                    updateSensorUpperThreshold(protocolParser.getSensor(), protocolParser.getValue());
                }
            } else {
                onDeviceRespondedWithError();
            }
        } catch (ProtocolException e) {
            onProtocolException();
        }
    }

    @Nullable
    private Sensor getSensorById(final Integer sensorId) {
        for (Integer i = 0; i < sensors.size(); i++) {
            Sensor sensor = sensors.get(i);
            if (sensor.getId().equals(sensorId)) {
                return sensor;
            }
        }

        return null;
    }

    private void updateSensorUi(final Integer sensorId, final BigDecimal value) {
        Sensor sensor = getSensorById(sensorId);
        if (sensor != null) {
            sensor.setValue(value);
        }
    }

    private void updateSensorLowerThreshold(final Integer sensorId, final BigDecimal value) {
        Sensor sensor = getSensorById(sensorId);
        if (sensor != null) {
            sensor.setLowerThreshold(value);
        }
    }

    private void updateSensorUpperThreshold(final Integer sensorId, final BigDecimal value) {
        Sensor sensor = getSensorById(sensorId);
        if (sensor != null) {
            sensor.setUpperThreshold(value);
        }
    }

    private void showDialogForActionThresholds(Integer sensorId) {
        Sensor sensor = getSensorById(sensorId);
        dialogView = null;
        dialogView = getLayoutInflater().inflate(R.layout.dialog_range, null);
        thresholdRangeView = dialogView.findViewById(R.id.fixed_rangeview);
        if (sensor != null) {
            thresholdRangeView.setStart(sensor.getLowerThreshold());
            thresholdRangeView.setEnd(sensor.getUpperThreshold());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.action_thresholds) + ": " + sensor.getName())
                    .setIcon(R.drawable.ic_settings_black_24dp)
                    .setView(dialogView)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // TODO
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LinearLayout rootLinearLayout = findViewById(R.id.root);

        progressBar = findViewById(R.id.activity_indicator);

        sensors = new ArrayList<>();

        sensors.add(new Sensor(MainActivity.this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogForActionThresholds(SENSOR_1);
            }
        }, SENSOR_1, getResources().getString(R.string.sensor1_title)));

        sensors.add(new Sensor(MainActivity.this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogForActionThresholds(SENSOR_2);
            }
        }, SENSOR_2, getResources().getString(R.string.sensor2_title)));

        sensors.add(new Sensor(MainActivity.this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogForActionThresholds(SENSOR_3);
            }
        }, SENSOR_3, getResources().getString(R.string.sensor3_title)));

        sensors.add(new Sensor(MainActivity.this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogForActionThresholds(SENSOR_4);
            }
        }, SENSOR_4, getResources().getString(R.string.sensor4_title)));

        for (Integer i = 0; i < sensors.size(); i++) {
            rootLinearLayout.addView(sensors.get(i).getElements());
        }
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
