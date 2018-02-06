package br.unioeste.cropmonitor.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.math.BigDecimal;

public class Sensor {

    private String name;

    private Integer id;

    private BigDecimal value;

    private Context context;

    private LinearLayout linearLayout;

    private TextView sensorTitle;

    private TextView sensorContent;

    public Sensor(Context context, Integer id, String name) {
        this.id = id;
        this.name = name;
        this.context = context;
        buildElements();
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

    public Sensor setName(String name) {
        this.name = name;
        this.sensorTitle.setText(name);

        return this;
    }

    public Sensor setValue(BigDecimal value) {
        this.value = value;
        this.sensorContent.setText(String.valueOf(value));

        return this;
    }
}
