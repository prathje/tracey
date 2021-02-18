package de.patrickrathje.tracey;

import android.content.Intent;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    public String shareData = null;     // The data that is being shared, e.g. a tracey group

    public ApduService() {
        Log.v(LOG_TAG, "Started!");
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        String d = shareData == null ? "" : shareData;

        Log.i(LOG_TAG, "Received APDU: " + HexConverter.ByteArrayToHexString(commandApdu));
        // If the APDU matches the SELECT AID command for this service,
        if (Arrays.equals(SELECT_APDU, commandApdu)) {
            Log.i(LOG_TAG, "Sharing Data: " + d);
            return ConcatArrays(d.getBytes(StandardCharsets.US_ASCII), SELECT_OK_SW);
        } else if (commandApdu[0] == 0 && commandApdu[1] == 0 && Arrays.equals(commandApdu, HexConverter.HexStringToByteArray("FFFFFF"))) {
            Log.i(LOG_TAG, "Sharing Data: " + d);
            return ConcatArrays(d.getBytes(StandardCharsets.US_ASCII), SELECT_OK_SW);
        } else {
            return UNKNOWN_CMD_SW;
        }

        // TODO: Use async sendResponseApdu(byte[]) ? Update the MainActivities State?!
    }

    @Override
    public void onDeactivated(int reason) {
        Log.v(MainActivity.LOG_TAG, "Deactivated reason:" + String.valueOf(reason));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction() != null) {
            if(intent.getAction().equals("setShareData")) {
                shareData = intent.getStringExtra("shareData");
                Log.i(LOG_TAG, "setShareData: " + shareData);
            } else if(intent.getAction().equals("resetShareData")) {
                Log.i(LOG_TAG, "resetShareData");
                shareData = null;
            } else if(intent.getAction().equals("test")) {
                onResult(HexConverter.HexStringToByteArray("F000000100000177AC91D6136FA0B35953546FBAD88E7C919246AEC5882EA4373DF3432E317E838BBDEFFF84B8898CB776776D6DFB6453C85A4CCC42DA8594773D598644DA36C0B09DA9C233"));
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }


   /* private void onInvitationSent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("on")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("result", HexConverter.ByteArrayToHexString(result));
        this.startActivity(intent);
    }*/

    private void onResult(byte[] result) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("onResult");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("result", HexConverter.ByteArrayToHexString(result));
        this.startActivity(intent);
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