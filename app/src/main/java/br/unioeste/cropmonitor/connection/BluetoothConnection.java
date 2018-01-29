package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnection {

    public static final String DEVICE_NAME = "LSCBLU";

    public static final Short MESSAGE_LEN = 8;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String APP_NAME = "CROPMONITOR_ANDROID";

    private BluetoothAdapter adapter;

    private BluetoothDevice pairedDevice;

    private ConnectThread connectThread = null;

    private ConnectedThread connectedThread = null;

    @NonNull
    public static IntentFilter getIntentFilterForActionState() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @NonNull
    public static IntentFilter getIntentFilterForBondState() {
        return new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
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

    public synchronized BluetoothConnection prepare() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        return this;
    }

    public BluetoothConnection init() {
        connectThread = new ConnectThread(pairedDevice);
        connectThread.start();

        return this;
    }

    private BluetoothConnection onConnected(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        return this;
    }

    public BluetoothConnection write(byte[] out) {
        connectedThread.write(out);

        return this;
    }

    public BluetoothConnection disconnect() {
        if (connectedThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        return this;
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket = null;

        ConnectThread(BluetoothDevice device) {
            pairedDevice = device;
        }

        public void run() {
            try {
                socket = pairedDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                try {
                    socket.connect();
                    onConnected(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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

        ConnectedThread(BluetoothSocket btSocket) {
            socket = btSocket;
            try {
                iStream = socket.getInputStream();
                oStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            byte[] buffer = new byte[MESSAGE_LEN];
            int bytesRead;
            while (true) {
                try {
                    if (iStream.available() >= MESSAGE_LEN) {
                        bytesRead = iStream.read(buffer, 0, MESSAGE_LEN);
                        if (bytesRead > 0) {
                            String incomingMessage = new String(buffer, 0, bytesRead);
                            // TODO: Enviar mensagem para activity inicial.
                        } else {
                            // PROTOCOL ERROR!
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        void write(byte[] bytes) {
            try {
                oStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
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
