package br.unioeste.cropmonitor.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
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
    private static final Integer THRESHOLD_PRECISION = 20;
    private String name;
    private Integer id;
    private BigDecimal value;
    private Context context;
    private LinearLayout rootLinearLayout;
    private TextView sensorTitle;
    private TextView sensorContent;
    private TextView sensorLowestValue;
    private TextView sensorHighestValue;
    private View.OnClickListener settingsAction;
    private Handler uiHandler;
    private Integer integerUpperThreshold;
    private Integer integerLowerThreshold;
    private BigDecimal decimalUpperThreshold;
    private BigDecimal decimalLowerThreshold;
    private BigDecimal lowestValue = new BigDecimal(Integer.MAX_VALUE).setScale(Protocol.DECIMAL_LEN, BigDecimal.ROUND_DOWN);
    private BigDecimal highestValue = new BigDecimal(Integer.MIN_VALUE).setScale(Protocol.DECIMAL_LEN, BigDecimal.ROUND_DOWN);

    public Sensor(Context context, View.OnClickListener settingsAction, Integer id, String name) {
        this.id = id;
        this.name = name;
        this.context = context;
        this.uiHandler = new Handler();
        this.integerUpperThreshold = LOWER_VOLTAGE;
        this.integerLowerThreshold = HIGHER_VOLTAGE;
        this.decimalLowerThreshold = integerToDecimalThreshold(LOWER_VOLTAGE);
        this.decimalUpperThreshold = integerToDecimalThreshold(HIGHER_VOLTAGE);
        this.settingsAction = settingsAction;
        buildElements();
    }

    @NonNull
    public static Integer decimalToIntegerThreshold(BigDecimal value) {
        return (int) Math.round((value.doubleValue() * THRESHOLD_PRECISION) / HIGHER_VOLTAGE);
    }

    @NonNull
    public static BigDecimal integerToDecimalThreshold(Integer value) {
        return new BigDecimal((value * HIGHER_VOLTAGE) / THRESHOLD_PRECISION).setScale(Protocol.DECIMAL_LEN, BigDecimal.ROUND_FLOOR);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
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

    private void buildElements() {
        rootLinearLayout = new LinearLayout(context);
        rootLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams rootParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rootParams.setMargins(10, 10, 10, 10);
        rootLinearLayout.setLayoutParams(rootParams);

        LinearLayout upperRowLinearLayout = new LinearLayout(context);
        upperRowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams upperParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        upperParams.setMargins(0, 0, 0, 10);
        upperRowLinearLayout.setLayoutParams(upperParams);


        LinearLayout bottomRowLinearLayout = new LinearLayout(context);
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

        sensorLowestValue = new TextView(context);
        sensorLowestValue.setGravity(Gravity.CENTER);
        sensorLowestValue.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        sensorLowestValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensorLowestValue.setAllCaps(false);
        sensorLowestValue.setTextSize(14);
        sensorLowestValue.setText("-");

        sensorContent = new TextView(context);
        sensorContent.setGravity(Gravity.CENTER);
        sensorContent.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        sensorContent.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensorContent.setAllCaps(false);
        sensorContent.setTextSize(18);
        sensorContent.setText("-");

        sensorHighestValue = new TextView(context);
        sensorHighestValue.setGravity(Gravity.CENTER);
        sensorHighestValue.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        sensorHighestValue.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        sensorHighestValue.setAllCaps(false);
        sensorHighestValue.setTextSize(14);
        sensorHighestValue.setText("-");

        Button settingsButton = new Button(context);
        settingsButton.setGravity(Gravity.CENTER);
        settingsButton.setText(R.string.edit);
        settingsButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        settingsButton.setTextSize(18);
        settingsButton.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_settings_black_24dp), null, null, null);
        settingsButton.setOnClickListener(settingsAction);

        upperRowLinearLayout.addView(sensorTitle);
        upperRowLinearLayout.addView(settingsButton);

        bottomRowLinearLayout.addView(sensorLowestValue);
        bottomRowLinearLayout.addView(sensorContent);
        bottomRowLinearLayout.addView(sensorHighestValue);

        rootLinearLayout.addView(upperRowLinearLayout);
        rootLinearLayout.addView(bottomRowLinearLayout);
    }

    public LinearLayout getElements() {
        return rootLinearLayout;
    }

    public Sensor setValue(BigDecimal newValue) {
        value = newValue;
        final Double thisDoubleValue = value.doubleValue();
        final Double highestDoubleValue = highestValue.doubleValue();
        final Double lowestDoubleValue = lowestValue.doubleValue();
        final Double upperThresholdDoubleValue = decimalUpperThreshold.doubleValue();
        final Double lowerThresholdDoubleValue = decimalLowerThreshold.doubleValue();

        if (thisDoubleValue > highestDoubleValue) {
            highestValue = value;
        }
        if (thisDoubleValue < lowestDoubleValue) {
            lowestValue = value;
        }

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sensorContent.setText(String.valueOf(value));
                sensorLowestValue.setText(String.valueOf(lowestValue));
                sensorHighestValue.setText(String.valueOf(highestValue));
                if (thisDoubleValue < lowerThresholdDoubleValue) {
                    // set color
                } else if (thisDoubleValue > upperThresholdDoubleValue) {
                    // set color
                } else {
                    // set color
                }
            }
        });

        return this;
    }

    public BigDecimal getDecimalLowerThreshold() {
        return decimalLowerThreshold;
    }

    public Integer getLowerThreshold() {
        return integerUpperThreshold;
    }

    public Sensor setLowerThreshold(BigDecimal value) {
        integerLowerThreshold = decimalToIntegerThreshold(value);
        decimalLowerThreshold = value;

        return this;
    }

    public BigDecimal getDecimalUpperThreshold() {
        return decimalUpperThreshold;
    }

    public Integer getUpperThreshold() {
        return integerUpperThreshold;
    }

    public Sensor setUpperThreshold(BigDecimal value) {
        integerLowerThreshold = decimalToIntegerThreshold(value);
        decimalUpperThreshold = value;

        return this;
    }

    public Sensor setLowerThreshold(Integer value) {
        integerLowerThreshold = value;
        decimalLowerThreshold = integerToDecimalThreshold(value);

        return this;
    }

    public Sensor setUpperThreshold(Integer value) {
        integerUpperThreshold = value;
        decimalUpperThreshold = integerToDecimalThreshold(value);

        return this;
    }

    public Sensor setThresholds(BigDecimal lower, BigDecimal upper) {
        integerUpperThreshold = decimalToIntegerThreshold(upper);
        integerLowerThreshold = decimalToIntegerThreshold(lower);
        decimalUpperThreshold = upper;
        decimalLowerThreshold = lower;

        return this;
    }

    public Sensor setThresholds(Integer lower, Integer upper) {
        integerUpperThreshold = upper;
        integerLowerThreshold = lower;
        decimalUpperThreshold = integerToDecimalThreshold(upper);
        decimalLowerThreshold = integerToDecimalThreshold(lower);

        return this;
    }
}
