package br.unioeste.cropmonitor.util;

import java.math.BigDecimal;

import br.unioeste.cropmonitor.util.exceptions.ProtocolException;

@SuppressWarnings({"StringBufferReplaceableByString", "WeakerAccess"})
public class Protocol {

    public static Short MESSAGE_LEN = 8;
    public static Short SENSOR_LEN = 1;
    public static Short INTEGER_LEN = 2;
    public static Short DECIMAL_LEN = 2;
    public static Short STATUS_LEN = 3;
    public static Short OPERATION_LEN = 1;

    public static Character OP_LOWER_THRESHOLD_UPDATE = 'L';
    public static Character OP_UPPER_THRESHOLD_UPDATE = 'U';
    public static Character OP_SENSOR_UPDATE = 'S';
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
    | OP_IDS:           L (Lower threshold update), U (Upper threshold update)
    |
    | Example:          ' L010050' --> Update lower threshold of sensor 01 to 00.50
    | Example:          ' U000350' --> Update upper threshold of sensor 00 to 03.50
    |
    */


    public Protocol(String message) throws ProtocolException {
        parseIntegrity(message);
        parseStatus(message.substring(0, STATUS_LEN));
        parseSensor(message.substring(STATUS_LEN, SENSOR_LEN));
        parseOperation(message.substring(SENSOR_LEN, OPERATION_LEN));
        parseValue(message.substring(STATUS_LEN, INTEGER_LEN + DECIMAL_LEN + 1).replaceAll(" ", "0"));
    }

//    @NonNull
//    public static String makeWriteString(Integer sensor, Double value) {
//        BigDecimal bg = BigDecimal.valueOf(value).setScale(DECIMAL_LEN, BigDecimal.ROUND_DOWN);
//        StringBuilder sensorString = new StringBuilder(MESSAGE_LEN);
//        sensorString.append(WRITE);
//        sensorString.append(String.format(Locale.ENGLISH, "%0" + SENSOR_LEN + "d", sensor));
//        sensorString.append(String.format(Locale.ENGLISH, "%0" + (INTEGER_LEN + DECIMAL_LEN + 1) + "." + DECIMAL_LEN + "f", bg));
//        return sensorString.toString();
//    }

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
            throw new ProtocolException("Invalid sensor.");
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
        value = new BigDecimal(fragment).setScale(DECIMAL_LEN, BigDecimal.ROUND_DOWN);
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
}
