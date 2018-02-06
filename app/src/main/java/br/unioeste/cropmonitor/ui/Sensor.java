package br.unioeste.cropmonitor.ui;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.math.BigDecimal;

import br.unioeste.cropmonitor.util.Protocol;

public class Sensor {

    private static final Integer LOWER_VOLTAGE = 0;
    private static final Integer HIGHER_VOLTAGE = 5;
    private String name;
    private Integer id;
    private BigDecimal value;
    private Context context;
    private LinearLayout linearLayout;
    private TextView sensorTitle;
    private TextView sensorContent;
    private Handler uiHandler;
    private BigDecimal lowerThreshold;
    private BigDecimal upperThreshold;
    private BigDecimal lowestValue = new BigDecimal(Integer.MAX_VALUE).setScale(Protocol.DECIMAL_LEN, BigDecimal.ROUND_DOWN);
    private BigDecimal highestValue = new BigDecimal(Integer.MIN_VALUE).setScale(Protocol.DECIMAL_LEN, BigDecimal.ROUND_DOWN);

    public Sensor(Context context, Integer id, String name) {
        this.id = id;
        this.name = name;
        this.context = context;
        this.uiHandler = new Handler();
        this.lowerThreshold = new BigDecimal(LOWER_VOLTAGE);
        this.upperThreshold = new BigDecimal(HIGHER_VOLTAGE);
        buildElements();
    }

    public Sensor(Context context, Integer id, String name, BigDecimal lowerThreshold, BigDecimal upperThreshold) {
        this.id = id;
        this.name = name;
        this.context = context;
        this.lowerThreshold = lowerThreshold;
        this.upperThreshold = upperThreshold;
        this.uiHandler = new Handler();
        buildElements();
    }

    public Integer getId() {
        return id;
    }

    private void buildElements() {
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        sensorTitle = new TextView(context);
        sensorTitle.setGravity(Gravity.CENTER);
        sensorTitle.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        sensorTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensorTitle.setAllCaps(false);
        sensorTitle.setTextSize(18);
        sensorTitle.setText(name);

        sensorContent = new TextView(context);
        sensorContent.setGravity(Gravity.CENTER);
        sensorContent.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        sensorContent.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensorContent.setAllCaps(false);
        sensorContent.setTextSize(18);
        sensorContent.setText("-");

        linearLayout.addView(sensorTitle);
        linearLayout.addView(sensorContent);
    }

    public LinearLayout getElements() {
        return linearLayout;
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