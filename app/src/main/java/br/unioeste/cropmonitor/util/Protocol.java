package br.unioeste.cropmonitor.util;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import br.unioeste.cropmonitor.util.exceptions.ProtocolException;

@SuppressWarnings({"StringBufferReplaceableByString", "WeakerAccess"})
public class Protocol {

    public static Short MESSAGE_LEN = 8;
    public static Short SENSOR_LEN = 2;
    public static Short INTEGER_LEN = 2;
    public static Short DECIMAL_LEN = 2;
    public static Short STATUS_LEN = 1;
    public static Short OPERATION_LEN = 1;

    public static Character OP_LOWER_THRESHOLD_UPDATE = 'L';
    public static Character OP_UPPER_THRESHOLD_UPDATE = 'U';
    public static Character OP_SENSOR_UPDATE = 'S';
    public static Character OP_REFRESH = 'R';
    public static Character OK = '+';
    public static Character ERROR = '-';

    private boolean ok = false;
    private boolean updateUpperThreshold = false;
    private boolean updateLowerThreshold = false;
    private boolean updateSensor = false;
    private BigDecimal value = new BigDecimal(0);
    private Integer sensor;

    /*
    |--------------------------------------------------------------------------
    | Protocol 8 bytes
    |--------------------------------------------------------------------------
    |
    | Incoming message: [STATUS(1)][OP_ID(1)][SENSOR(2)][INTEGER(2)][DECIMAL(2)]
    | OP_IDS:           S (Sensor update), L (Lower threshold update), U (Upper threshold update)
    | STATUS:           + (Success), - (Error)
    |
    | Example:          '+S000322' --> Sensor 00 responded with success and value 03.22
    | Example:          '-S010000' --> Sensor 01 responded with error
    | Example:          '+L010100' --> Lower threshold of sensor 01 updated to 01.00 with success
    | Example:          '-U000000' --> Upper threshold of sensor 00 cannot be updated
    |
    | Outgoing message: [ANY(1)][OP_ID(1)][SENSOR(2)][INTEGER(2)][DECIMAL(2)]
    | OP_IDS:           L (Lower threshold update), U (Upper threshold update), R (Refresh sensors thresholds)
    |
    | Example:          ' L010050' --> Update lower threshold of sensor 01 to 00.50
    | Example:          ' U000350' --> Update upper threshold of sensor 00 to 03.50
    | Example:          ' R000000' --> Sync all sensors thresholds
    |
    */

    public Protocol(String message) throws ProtocolException {
        parseIntegrity(message);
        parseStatus(fragmenter(message, 0, Integer.valueOf(STATUS_LEN)));
        parseOperation(fragmenter(message, Integer.valueOf(STATUS_LEN), Integer.valueOf(OPERATION_LEN)));
        parseSensor(fragmenter(message, STATUS_LEN + OPERATION_LEN, Integer.valueOf(SENSOR_LEN)));
        parseValue(fragmenter(message, STATUS_LEN + OPERATION_LEN + SENSOR_LEN, INTEGER_LEN + DECIMAL_LEN));
    }

    @NonNull
    public static String makeRequestSyncMessage() {
        StringBuilder builder = new StringBuilder(MESSAGE_LEN);
        builder.append(' ');
        builder.append(OP_REFRESH);
        for (int i = OPERATION_LEN + 1; i < MESSAGE_LEN; i++) {
            builder.append('0');
        }
        return builder.toString();
    }

    @NonNull
    public static String makeUpdateThresholdMessage(final Integer sensorId, boolean lowerNotUpper, BigDecimal value) {
        StringBuilder builder = new StringBuilder(MESSAGE_LEN);
        builder.append(' ');
        builder.append(lowerNotUpper ? OP_LOWER_THRESHOLD_UPDATE : OP_UPPER_THRESHOLD_UPDATE);
        builder.append(String.format("%02d", sensorId));
        builder.append(value.toString());
        return builder.toString();
    }

    @NonNull
    private String fragmenter(String input, Integer start, Integer length) {
        return input.substring(start, Math.min(start + length, MESSAGE_LEN));
    }

    public void parseIntegrity(String message) throws ProtocolException {
        if (message.length() != MESSAGE_LEN) {
            throw new ProtocolException("Invalid message length.");
        }
    }

    private void parseStatus(String fragment) throws ProtocolException {
        Character status = fragment.charAt(0);
        if (status.equals(OK)) {
            ok = true;
        } else if (status.equals(ERROR)) {
            ok = false;
        } else {
            throw new ProtocolException("Invalid status code.");
        }
    }

    private void parseSensor(String fragment) throws ProtocolException {
        try {
            sensor = Integer.valueOf(fragment);
        } catch (NumberFormatException e) {
            throw new ProtocolException("Invalid sensor: " + fragment);
        }
    }

    private void parseOperation(String fragment) throws ProtocolException {
        Character operation = fragment.charAt(0);
        if (operation.equals(OP_SENSOR_UPDATE)) {
            updateSensor = true;
        } else if (operation.equals(OP_LOWER_THRESHOLD_UPDATE)) {
            updateLowerThreshold = true;
        } else if (operation.equals(OP_UPPER_THRESHOLD_UPDATE)) {
            updateUpperThreshold = true;
        } else {
            throw new ProtocolException("Invalid operation.");
        }
    }

    private void parseValue(String fragment) {
        StringBuilder builder = new StringBuilder(INTEGER_LEN + DECIMAL_LEN + 1);
        builder.append(fragmenter(fragment, 0, Integer.valueOf(INTEGER_LEN)));
        builder.append('.');
        builder.append(fragmenter(fragment, Integer.valueOf(INTEGER_LEN), Integer.valueOf(DECIMAL_LEN)));
        value = new BigDecimal(builder.toString()).setScale(DECIMAL_LEN, BigDecimal.ROUND_DOWN);
    }

    public boolean ok() {
        return ok;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Integer getSensor() {
        return sensor;
    }

    public boolean isUpdateLowerThreshold() {
        return updateLowerThreshold;
    }

    public boolean isUpdateSensor() {
        return updateSensor;
    }

    public boolean isUpdateUpperThreshold() {
        return updateUpperThreshold;
    }

    public String toString() {
        return "{\"ok\": " + ok +
                ", \"value\": " + value.doubleValue() +
                ", \"is_update_sensor\": " + updateSensor +
                ", \"is_update_lower_threshold\": " + updateLowerThreshold +
                ", \"is_update_upper_threshold\": " + updateUpperThreshold +
                ", \"sensor\": " + sensor +
                "}";
    }
}
