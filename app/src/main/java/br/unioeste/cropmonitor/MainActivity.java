package br.unioeste.cropmonitor;

import android.bluetooth.BluetoothAdapter;
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

    private Button btnStart;

    private TextView sensor1Content;

    private Handler uiHandler = new Handler();

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

    private void requestBluetoothEnablement() {
        Intent enableIntent = bluetoothConnection.getIntentForEnabling();
        startActivityForResult(enableIntent, REQUEST_ENABLE);
    }

    private void connect() {
        bluetoothConnection = new BluetoothConnection();

        try {
            bluetoothConnection.initializeAdapter();
            registerReceiver(broadcastActionState, bluetoothConnection.getIntentFilterForActionState());
            if (!bluetoothConnection.isEnabled()) {
                requestBluetoothEnablement();
            }

            // onConnected()
            // onErrorConnection()

            if (bluetoothConnection.isEnabled()) {
                bluetoothConnection.attemptPair();
                if (bluetoothConnection.isPaired()) {
                    onConnected();
                } else {
                    onErrorConnection("Connection is not ready.");
                }
            } else {
                onErrorConnection("Connection is not enabled.");
            }
        } catch (IOException e) {
            onUnsupportedDevice("Initialization thrown IOException.");
            e.printStackTrace();
        }
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

        btnStart = findViewById(R.id.startPollingBtn);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(false);
                poll();
            }
        });

        broadcastActionState = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    Integer state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            //
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            //
                            break;
                        case BluetoothAdapter.STATE_ON:
                            //
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            //
                            break;
                    }
                }
            }
        };

        connect();
    }

    public void poll() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateSensors();
            }
        };
        new Thread(runnable).start();
    }

    private void updateSensor1Ui(final String value) {
        sensor1Content = findViewById(R.id.sensor1Content);
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sensor1Content.setText(value);
            }
        });
    }

    private void updateSensors() {
        Integer sensor1;
        for (; ; ) {
            sensor1 = new Random().nextInt();
            updateSensor1Ui(String.valueOf(sensor1) + " V");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastActionState);
    }
}
