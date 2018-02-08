package br.unioeste.cropmonitor.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.math.BigDecimal;

import br.unioeste.cropmonitor.R;
import br.unioeste.cropmonitor.util.Protocol;

public class Sensor {

    private static final Integer LOWER_VOLTAGE = 0;
    private static final Integer HIGHER_VOLTAGE = 5;
    private String name;
    private Integer id;
    private BigDecimal value;
    private Context context;
    private LayoutInflater inflater;
    private LinearLayout rootLinearLayout;
    private LinearLayout upperRowLinearLayout;
    private LinearLayout bottomRowLinearLayout;
    private TextView sensorTitle;
    private TextView sensorContent;
    private Button settingsButton;
    private Handler uiHandler;
    private BigDecimal lowerThreshold;
    private BigDecimal upperThreshold;
    private BigDecimal lowestValue = new BigDecimal(Integer.MAX_VALUE).setScale(Protocol.DECIMAL_LEN, BigDecimal.ROUND_DOWN);
    private BigDecimal highestValue = new BigDecimal(Integer.MIN_VALUE).setScale(Protocol.DECIMAL_LEN, BigDecimal.ROUND_DOWN);

    public Sensor(Context context, LayoutInflater inflater, Integer id, String name) {
        this.id = id;
        this.name = name;
        this.context = context;
        this.uiHandler = new Handler();
        this.lowerThreshold = new BigDecimal(LOWER_VOLTAGE);
        this.upperThreshold = new BigDecimal(HIGHER_VOLTAGE);
        this.inflater = inflater;
        buildElements();
    }

    public Sensor(Context context, LayoutInflater inflater, Integer id, String name, BigDecimal lowerThreshold, BigDecimal upperThreshold) {
        this.id = id;
        this.name = name;
        this.context = context;
        this.lowerThreshold = lowerThreshold;
        this.upperThreshold = upperThreshold;
        this.uiHandler = new Handler();
        this.inflater = inflater;
        buildElements();
    }

    public Integer getId() {
        return id;
    }

    private void showDialogForLowerThreshold() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.action_thresholds)
                .setIcon(R.drawable.ic_settings_black_24dp)
                .setView(inflater.inflate(R.layout.dialog_range, null))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void buildElements() {
        rootLinearLayout = new LinearLayout(context);
        rootLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rootParams.setMargins(10, 10, 10, 10);
        rootLinearLayout.setLayoutParams(rootParams);

        upperRowLinearLayout = new LinearLayout(context);
        upperRowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams upperParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        upperParams.setMargins(0, 0, 0, 10);
        upperRowLinearLayout.setLayoutParams(upperParams);


        bottomRowLinearLayout = new LinearLayout(context);
        bottomRowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams bottomParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        bottomParams.setMargins(0, 0, 0, 20);
        bottomRowLinearLayout.setLayoutParams(bottomParams);

        sensorTitle = new TextView(context);
        sensorTitle.setGravity(Gravity.CENTER);
        sensorTitle.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        sensorTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensorTitle.setAllCaps(true);
        sensorTitle.setTypeface(null, Typeface.BOLD);
        sensorTitle.setTextSize(18);
        sensorTitle.setText(name);

        sensorContent = new TextView(context);
        sensorContent.setGravity(Gravity.CENTER);
        sensorContent.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        sensorContent.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensorContent.setAllCaps(false);
        sensorContent.setTextSize(18);
        sensorContent.setText("-");

        settingsButton = new Button(context);
        settingsButton.setGravity(Gravity.CENTER);
        settingsButton.setText(R.string.edit);
        settingsButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        settingsButton.setTextSize(18);
        settingsButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_settings_black_24dp), null, null, null);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogForLowerThreshold();
            }
        });

        upperRowLinearLayout.addView(sensorTitle);
        upperRowLinearLayout.addView(settingsButton);

        bottomRowLinearLayout.addView(sensorContent);

        rootLinearLayout.addView(upperRowLinearLayout);
        rootLinearLayout.addView(bottomRowLinearLayout);
    }

    public LinearLayout getElements() {
        return rootLinearLayout;
    }

    public Sensor setName(String newName) {
        name = newName;

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sensorTitle.setText(name);
            }
        });

        return this;
    }

    public Sensor setValue(BigDecimal newValue) {
        value = newValue;
        if (value.doubleValue() > highestValue.doubleValue()) {
            highestValue = value;
        }
        if (value.doubleValue() < lowestValue.doubleValue()) {
            lowestValue = value;
        }

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sensorContent.setText(String.valueOf(value));
            }
        });

        return this;
    }

    public BigDecimal getLowerThreshold() {
        return lowerThreshold;
    }

    public Sensor setLowerThreshold(BigDecimal newValue) {
        lowerThreshold = newValue;

        return this;
    }

    public BigDecimal getUpperThreshold() {
        return upperThreshold;
    }

    public Sensor setUpperThreshold(BigDecimal newValue) {
        upperThreshold = newValue;

        return this;
    }
}
