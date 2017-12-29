package br.unioeste.cropmonitor;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


public class MonitorFragment extends Fragment {

    Button btnStart;
    private Handler uiHandler = new Handler();
    private TextView sensor1Content;
    private OnFragmentInteractionListener mListener;

    public MonitorFragment() {
        // Required empty public constructor
    }

    public static MonitorFragment newInstance() {
        MonitorFragment fragment = new MonitorFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);
        btnStart = view.findViewById(R.id.startPollingBtn);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStart.setEnabled(false);
                poll(view);
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void poll(View view) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateSensors();
            }
        };
        new Thread(runnable).start();
    }

    private void updateSensor1Ui(final String value) {
        sensor1Content = getView().findViewById(R.id.sensor1Content);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
