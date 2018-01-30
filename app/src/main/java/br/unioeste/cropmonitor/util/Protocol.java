package br.unioeste.cropmonitor.util;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Locale;

public class Protocol {

    public static Short MESSAGE_LEN = 8;
    public static Short SENSOR_LEN = 2;
    public static Short VALUE_LEN = 2;
    public static Short DECIMAL_LEN = 2;
    public static Character READ = 'R';
    public static Character WRITE = 'W';
    private boolean ok = false;
    private BigDecimal value = new BigDecimal(0);

    public Protocol(String message) {
        parseMessage(message);
    }

    @NonNull
    public static String makeReadString(Integer sensor) throws Exception {
        StringBuilder sensorString = new StringBuilder(MESSAGE_LEN);
        sensorString.append(READ);
        sensorString.append(String.format(Locale.ENGLISH, "%0" + SENSOR_LEN + "d", sensor));
        sensorString.append(String.format(Locale.ENGLISH, "%" + VALUE_LEN + "." + DECIMAL_LEN + "f", (float) 0));
        return sensorString.toString();
    }

    @NonNull
    public static String makeWriteString(Integer sensor, Double value) throws Exception {
        BigDecimal bg = BigDecimal.valueOf(value).setScale(DECIMAL_LEN, BigDecimal.ROUND_DOWN);
        StringBuilder sensorString = new StringBuilder(MESSAGE_LEN);
        sensorString.append(WRITE);
        sensorString.append(String.format(Locale.ENGLISH, "%0" + SENSOR_LEN + "d", sensor));
        sensorString.append(String.format(Locale.ENGLISH, "%" + VALUE_LEN + "." + DECIMAL_LEN + "f", bg));
        return sensorString.toString();
    }

    private void parseMessage(String message) {

    }
}
