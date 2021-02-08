package de.patrickrathje.tracey;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

public class ApduService extends HostApduService {


    public static final String LOG_TAG = "Tracey";

    public static final String AID = "F0010203040506";
    public static final String SELECT_APDU_HEADER = "00A40400";
    public static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    public static final byte[] UNKNOWN_CMD_SW = HexStringToByteArray("0000");
    public static final byte[] SELECT_APDU = BuildSelectApdu(AID);

    public static final byte[] EPH_KEY = new byte[64];

    public ApduService() {
        Log.v(LOG_TAG, "Started!");
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

        Log.i(LOG_TAG, "Received APDU: " + ByteArrayToHexString(commandApdu));
        // If the APDU matches the SELECT AID command for this service,
        // send the loyalty card account number, followed by a SELECT_OK status trailer (0x9000).
        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            Log.i(LOG_TAG, "Sending 2: " + ByteArrayToHexString(EPH_KEY));
            return ConcatArrays(EPH_KEY, SELECT_OK_SW);
        } else if (commandApdu[0] == 0 && commandApdu[1] == 0 && Arrays.equals(commandApdu, ApduService.HexStringToByteArray("FFFFFF"))) {
            Log.i(LOG_TAG, "Sending 2: " + ByteArrayToHexString(EPH_KEY));
            return ConcatArrays(EPH_KEY, SELECT_OK_SW);
        } else {
            return UNKNOWN_CMD_SW;
        }

        // TODO: Use async sendResponseApdu(byte[]) ? Update the MainActivities State?!
    }

    @Override
    public void onDeactivated(int reason) {
        Log.v(MainActivity.LOG_TAG, "Deactivated reason:" + String.valueOf(reason));
    }

    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    /**
     * Utility method to convert a hexadecimal string to a byte string.
     *
     * <p>Behavior with input strings containing non-hexadecimal characters is undefined.
     *
     * @param s String containing hexadecimal characters to convert
     * @return Byte array generated from input
     * @throws java.lang.IllegalArgumentException if input length is incorrect
     */
    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Utility method to concatenate two byte arrays.
     * @param first First array
     * @param rest Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Build APDU for SELECT AID command. This command indicates which service a reader is
     * interested in communicating with. See ISO 7816-4.
     *
     * @param aid Application ID (AID) to select
     * @return APDU for SELECT AID command
     */
    public static byte[] BuildSelectApdu(String aid) {
        // Format: [CLASS | INSTRUCTION | PARAMETER 1 | PARAMETER 2 | LENGTH | DATA]
        return HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
    }
}