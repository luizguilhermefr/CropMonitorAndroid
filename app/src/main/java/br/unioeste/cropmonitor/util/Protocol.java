package br.unioeste.cropmonitor.util;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Locale;

import br.unioeste.cropmonitor.util.exceptions.ProtocolException;

public class Protocol {

    public static Short MESSAGE_LEN = 8;
    public static Short SENSOR_LEN = 2;
    public static Short INTEGER_LEN = 2;
    public static Short DECIMAL_LEN = 2;
    public static Short STATUS_LEN = 3;
    public static Short VALUE_LEN = 5;
    public static Character READ = 'R';
    public static Character WRITE = 'W';
    public static String OK = "OK+";
    public static String ERROR = "ER-";
    private boolean ok = false;
    private BigDecimal value = new BigDecimal(0);

    public Protocol(String message) throws ProtocolException {
        parseMessage(message);
    }

    @NonNull
    public static String makeReadString(Integer sensor) throws Exception {
        StringBuilder sensorString = new StringBuilder(MESSAGE_LEN);
        sensorString.append(READ);
        sensorString.append(String.format(Locale.ENGLISH, "%0" + SENSOR_LEN + "d", sensor));
        sensorString.append(String.format(Locale.ENGLISH, "%" + INTEGER_LEN + "." + DECIMAL_LEN + "f", (float) 0));
        return sensorString.toString();
    }

    @NonNull
    public static String makeWriteString(Integer sensor, Double value) throws Exception {
        BigDecimal bg = BigDecimal.valueOf(value).setScale(DECIMAL_LEN, BigDecimal.ROUND_DOWN);
        StringBuilder sensorString = new StringBuilder(MESSAGE_LEN);
        sensorString.append(WRITE);
        sensorString.append(String.format(Locale.ENGLISH, "%0" + SENSOR_LEN + "d", sensor));
        sensorString.append(String.format(Locale.ENGLISH, "%" + INTEGER_LEN + "." + DECIMAL_LEN + "f", bg));
        return sensorString.toString();
    }

    private void parseStatus(String status) throws ProtocolException {
        if (status.equals(OK)) {
            ok = true;
        } else if (status.equals(ERROR)) {
            ok = false;
        } else {
            throw new ProtocolException("Invalid status code.");
        }
    }

    private void parseValue(String content) {
        value = new BigDecimal(content).setScale(DECIMAL_LEN, BigDecimal.ROUND_DOWN);
    }

    private void parseMessage(String message) throws ProtocolException {
        parseStatus(message.substring(0, STATUS_LEN - 1));
        parseValue(message.substring(STATUS_LEN, VALUE_LEN - 1));
    }

    public boolean ok() {
        return ok;
    }

    public BigDecimal getValue() {
        return value;
    }
}
