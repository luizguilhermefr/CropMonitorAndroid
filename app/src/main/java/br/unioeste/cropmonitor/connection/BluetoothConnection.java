package br.unioeste.cropmonitor.connection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnection {

    public static final String DEVICE_NAME = "LSCBLU";

    public static final Short MESSAGE_LEN = 8;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final String APP_NAME = "CROPMONITOR_ANDROID";

    private BluetoothAdapter adapter;

    private BluetoothDevice pairedDevice;

    private UUID pairedDeviceUUID;

    private AcceptThread acceptThread = null;

    private ConnectThread connectThread = null;

    private ConnectedThread connectedThread = null;

    public IntentFilter getIntentFilterForActionState() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public IntentFilter getIntentFilterForBondState() {
        return new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
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

    public BluetoothConnection enableAdapter() {
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
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

        return this;
    }

    public BluetoothConnection init() {
        connectThread = new ConnectThread(pairedDevice, MY_UUID_INSECURE);
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

    private class AcceptThread extends Thread {
        private BluetoothServerSocket listenerSocket = null;

        public AcceptThread() {
            try {
                listenerSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID_INSECURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            try {
                socket = listenerSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (socket != null) {
                onConnected(socket);
            }
        }

        public void cancel() {
            try {
                listenerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket = null;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            pairedDevice = device;
            pairedDeviceUUID = uuid;
        }

        public void run() {
            try {
                socket = pairedDevice.createRfcommSocketToServiceRecord(pairedDeviceUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            adapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            onConnected(socket);
        }

        public void cancel() {
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

        public ConnectedThread(BluetoothSocket btSocket) {
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
            int bytes;
            while (true) {
                try {
                    bytes = iStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    // TODO: Enviar mensagem para activity inicial.
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                oStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
