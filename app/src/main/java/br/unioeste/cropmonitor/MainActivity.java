package br.unioeste.cropmonitor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import br.unioeste.cropmonitor.connection.Bluethooth;
import br.unioeste.cropmonitor.connection.contracts.ConnectionInterface;

public class MainActivity extends AppCompatActivity implements MonitorFragment.OnFragmentInteractionListener {

    ConnectionInterface connection;

    public final static int REQUEST_ENABLE = 1;

    private OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ft.replace(R.id.mainFrame, new MonitorFragment()).commit();
                    return true;
                case R.id.navigation_dashboard:
                    System.out.println("not implemented yet.");
                    return false;
                case R.id.navigation_notifications:
                    System.out.println("not implemented yet.");
                    return false;
            }
            return false;
        }
    };

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

    private void connect() {
        connection = new Bluethooth();
        try {
            connection.init();
            if (!connection.isEnabled()) {
                Intent enableIntent = connection.requestEnablement();
                startActivityForResult(enableIntent, REQUEST_ENABLE);
            }
            if (connection.isEnabled()) {
                connection.connect();
                if (connection.isReady()) {
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

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, new MonitorFragment()).commit();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

}
