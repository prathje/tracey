package de.patrickrathje.tracey;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;

import de.patrickrathje.tracey.utils.HexConverter;

public class ApduService extends HostApduService {

    public static final String LOG_TAG = "Tracey";

    public static final String AID = "F0010203040506";
    public static final String SELECT_APDU_HEADER = "00A40400";
    public static final byte[] SELECT_OK_SW = HexConverter.HexStringToByteArray("9000");
    public static final byte[] UNKNOWN_CMD_SW = HexConverter.HexStringToByteArray("0000");
    public static final byte[] SELECT_APDU = BuildSelectApdu(AID);

    public static final byte[] EPH_KEY = new byte[64];

    public ApduService() {
        Log.v(LOG_TAG, "Started!");
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

        Log.i(LOG_TAG, "Received APDU: " + HexConverter.ByteArrayToHexString(commandApdu));
        // If the APDU matches the SELECT AID command for this service,
        // send the loyalty card account number, followed by a SELECT_OK status trailer (0x9000).
        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            Log.i(LOG_TAG, "Sending 2: " + HexConverter.ByteArrayToHexString(EPH_KEY));
            return ConcatArrays(EPH_KEY, SELECT_OK_SW);
        } else if (commandApdu[0] == 0 && commandApdu[1] == 0 && Arrays.equals(commandApdu, HexConverter.HexStringToByteArray("FFFFFF"))) {
            Log.i(LOG_TAG, "Sending 2: " + HexConverter.ByteArrayToHexString(EPH_KEY));
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
        return HexConverter.HexStringToByteArray(SELECT_APDU_HEADER + String.format("%02X",
                aid.length() / 2) + aid);
    }
}