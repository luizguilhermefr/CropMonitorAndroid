package br.unioeste.cropmonitor.connection.contracts;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;

public interface ConnectionInterface {
    void init() throws IOException;

    boolean isEnabled();

    Intent requestEnablement();

    BroadcastReceiver getStatusReceiver();

    public IntentFilter getBroadcastFilter();
}
