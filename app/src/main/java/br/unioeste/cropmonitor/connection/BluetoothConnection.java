package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import br.unioeste.cropmonitor.util.Protocol;

@SuppressWarnings("UnusedReturnValue")
public class BluetoothConnection {

    public static final String DEVICE_NAME = "LSCBLU";
    public static final String CONNECTION = "CONNECTION";
    public static final String STATUS = "STATUS";
    public static final int STATUS_FAILURE = -1;
    public static final int STATUS_SOCKET_FAILURE = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_OK = 2;
    public static final String UPDATE = "UPDATE";
    public static final String SENSOR = "SENSOR";
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter adapter;
    private BluetoothDevice pairedDevice;
    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;
    private Context context = null;

    public BluetoothConnection(Context ctx) {
        context = ctx;
    }

    @NonNull
    public static IntentFilter getIntentFilterForActionState() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @NonNull
    public static IntentFilter getIntentFilterForBondState() {
        return new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    }

    @NonNull
    public static IntentFilter getIntentFilterForConnect() {
        return new IntentFilter(CONNECTION);
    }

    @NonNull
    public static IntentFilter getIntentFilterForUpdate() {
        return new IntentFilter(UPDATE);
    }

    @NonNull
    public static Intent getIntentForEnabling() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    public BluetoothDevice getBondedDevice() {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(DEVICE_NAME)) {
                    return device;
                }
            }
        }

        return null;
    }

    public BluetoothConnection checkAdapter() throws IOException {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            throw new IOException("Device doesn't support BluetoothConnection connection.");
        }

        return this;
    }

    public boolean isEnabled() {
        return adapter.isEnabled();
    }

    public BluetoothConnection forceEnable() {
        if (!adapter.isEnabled()) {
            adapter.enable();
        }

        return this;
    }

    public BluetoothConnection setPairedDevice(BluetoothDevice device) {
        pairedDevice = device;

        return this;
    }

    private void killConnectThread() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    private void killConnectedThread() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    public synchronized BluetoothConnection initialize() {
        disconnect();

        connectThread = new ConnectThread();
        connectThread.start();

        return this;
    }

    public Boolean connected() {
        return (connectedThread != null && connectedThread.isAlive());
    }

    public BluetoothConnection write(byte[] out) {
        connectedThread.write(out);

        return this;
    }

    public void disconnect() {
        killConnectThread();
        killConnectedThread();
    }

    private void onConnecting() {
        Intent connectIntent = new Intent(CONNECTION);
        connectIntent.putExtra(STATUS, STATUS_WORKING);
        LocalBroadcastManager.getInstance(context).sendBroadcast(connectIntent);
    }

    private void onConnected() {
        Intent connectIntent = new Intent(CONNECTION);
        connectIntent.putExtra(STATUS, STATUS_OK);
        LocalBroadcastManager.getInstance(context).sendBroadcast(connectIntent);
    }

    private void onCannotConnect() {
        Intent connectIntent = new Intent(CONNECTION);
        connectIntent.putExtra(STATUS, STATUS_FAILURE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(connectIntent);
    }

    private void onSocketError() {
        Intent connectIntent = new Intent(CONNECTION);
        connectIntent.putExtra(STATUS, STATUS_SOCKET_FAILURE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(connectIntent);
    }

    private void onMessageArrived(String message) {
        Intent updateIntent = new Intent(UPDATE);
        updateIntent.putExtra(SENSOR, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket = null;

        public void run() {
            try {
                onConnecting();
                socket = pairedDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                try {
                    socket.connect();
                    connectedThread = new ConnectedThread(socket);
                    if (connectedThread.ready()) {
                        connectedThread.start();
                    }
                } catch (IOException e) {
                    onCannotConnect();
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (IOException e) {
                onCannotConnect();
            }

        }

        void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private InputStream iStream = null;
        private OutputStream oStream = null;
        private Boolean readyToStart = false;

        ConnectedThread(BluetoothSocket btSocket) {
            socket = btSocket;
            try {
                iStream = socket.getInputStream();
                oStream = socket.getOutputStream();
                readyToStart = true;
                onConnected();
            } catch (IOException e) {
                onSocketError();
            }
        }

        Boolean ready() {
            return readyToStart;
        }

        public void run() {
            byte[] buffer = new byte[Protocol.MESSAGE_LEN];
            int bytesRead;
            while (true) {
                try {
                    if (iStream.available() >= Protocol.MESSAGE_LEN) {
                        bytesRead = iStream.read(buffer, 0, Protocol.MESSAGE_LEN);
                        if (bytesRead > 0) {
                            String incomingMessage = new String(buffer, 0, bytesRead);
                            onMessageArrived(incomingMessage);
                        } else {
                            // PROTOCOL ERROR!
                        }
                    }
                } catch (IOException e) {
                    onSocketError();
                    break;
                }
            }
        }

        void write(byte[] bytes) {
            try {
                oStream.write(bytes);
            } catch (IOException e) {
                onSocketError();
            }
        }

        void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
