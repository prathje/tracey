package de.patrickrathje.tracey;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {


    public static final String LOG_TAG = "Tracey";

    public final int READER_MODE_MIN_TIME = 200;
    public final int READER_MODE_MAX_TIME = 300;

    public final int PRESENTER_MODE_MIN_TIME = 100;
    public final int PRESENTER_MODE_MAX_TIME = 100;

    public final int STATE_PRESENTING_HCE_IN_BACKGROUND = 0;
    public final int STATE_PRESENTING_HCE_IN_FOREGROUND = 1;
    public final int STATE_SEARCHING_FOR_HCE = 2;
    public final int STATE_CONNECTED_IN_BACKGROUND = 3;
    public final int STATE_CONNECTED_IN_FOREGROUND = 4;

    private int state = STATE_PRESENTING_HCE_IN_BACKGROUND;

    private boolean isReaderModeEnabled = false;

    NfcAdapter nfcAdapter = null;

    final Random random = new Random();
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_settings, R.id.navigation_contacts)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            nfcAdapter.setNdefPushMessage(null, this); // Disable BEAM for this activity
        }

        startService(new Intent(this, ApduService.class));


        final Random random = new Random();
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        this.updateReaderMode(STATE_CONNECTED_IN_FOREGROUND);

        IsoDep isoDep = IsoDep.get(tag);

        if (isoDep != null) {
            try {
                // Connect to the remote NFC device
                isoDep.connect();
                // Build SELECT AID command for our loyalty card service.
                // This command tells the remote device which service we wish to communicate with.
                Log.i("Tracey", "Requesting remote AID: " + ApduService.AID);
                byte[] command = ApduService.SELECT_APDU;

                // Send command to remote device
                Log.i("Tracey", "Sending: " + ApduService.ByteArrayToHexString(command));
                byte[] result = isoDep.transceive(command);
                // If AID is successfully selected, 0x9000 is returned as the status word (last 2
                // bytes of the result) by convention. Everything before the status word is
                // optional payload, which is used here to hold the account number.
                int resultLength = result.length;
                byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
                byte[] payload = Arrays.copyOf(result, resultLength-2);
                if (Arrays.equals(ApduService.SELECT_OK_SW, statusWord)) {
                    // The remote NFC device will immediately respond with its stored account number
                    String accountNumber = new String(payload, "UTF-8");
                    Log.i("Tracey", "Received: " + accountNumber);

                    Log.i("Tracey", "Sending own key...");
                    result = isoDep.transceive(ApduService.EPH_KEY);

                    resultLength = result.length;
                    byte[] statusWord2 = {result[resultLength-2], result[resultLength-1]};
                    payload = Arrays.copyOf(result, resultLength-2);
                    if (Arrays.equals(ApduService.SELECT_OK_SW, statusWord2)) {
                        accountNumber = new String(payload, "UTF-8");
                        Log.i("Tracey", "Received2: " + accountNumber);
                    } else {
                        Log.i("Tracey", "Received data 2 was not okay: " + ApduService.ByteArrayToHexString(result));
                    }

                } else {
                    Log.i("Tracey", "Received data was not okay: " + ApduService.ByteArrayToHexString(result));
                }
            } catch (IOException e) {
                Log.e("Tracey", "Error communicating with card: " + e.toString());
            }
        }

        // in case of failure, it might be that the other party just enabled reader mode
        // in case of success, we disable reader mode for now TODO: Is that correct?
        this.updateReaderMode(STATE_PRESENTING_HCE_IN_FOREGROUND);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateReaderMode(STATE_SEARCHING_FOR_HCE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.updateReaderMode(STATE_PRESENTING_HCE_IN_BACKGROUND);
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    protected synchronized void startReaderMode() {
        if (nfcAdapter != null &&  !isReaderModeEnabled) {
            //Log.v("Tracey", "Starting Reader Mode");
            nfcAdapter.enableReaderMode(this, this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null);
            isReaderModeEnabled = true;
        }
    }

    protected synchronized void stopReaderMode() {
        if (nfcAdapter != null && isReaderModeEnabled) {
            //Log.v("Tracey", "Stopping Reader Mode");
            nfcAdapter.disableReaderMode(this);
            isReaderModeEnabled = false;
        }
    }

    public synchronized void updateReaderMode(int newState) {
        state = newState;
        switch (state) {
            case STATE_PRESENTING_HCE_IN_BACKGROUND:
                stopReaderMode(); // Stop the reader mode entirely
                break;
            case STATE_PRESENTING_HCE_IN_FOREGROUND:
                stopReaderMode(); // Stop the reader mode for a moment
                break;
            case STATE_SEARCHING_FOR_HCE:
                startReaderMode(); // Start the reader mode for a moment
                break;
            case STATE_CONNECTED_IN_BACKGROUND:
            case STATE_CONNECTED_IN_FOREGROUND:
                break; // We do not change anything as we are transmitting!
        }

        if (state == STATE_PRESENTING_HCE_IN_FOREGROUND) {
            handler.postDelayed(this::fromPresentingToSearching, PRESENTER_MODE_MIN_TIME+random.nextInt(1+PRESENTER_MODE_MAX_TIME-PRESENTER_MODE_MIN_TIME));
        }

        if (state == STATE_SEARCHING_FOR_HCE) {
            handler.postDelayed(this::fromSearchingToPresenting, READER_MODE_MIN_TIME+random.nextInt(1+READER_MODE_MAX_TIME-READER_MODE_MIN_TIME));
        }
    }

    protected synchronized void fromPresentingToSearching() {
        if (state == STATE_PRESENTING_HCE_IN_FOREGROUND) {
            this.updateReaderMode(STATE_SEARCHING_FOR_HCE);
        }
    }


    protected synchronized void fromSearchingToPresenting() {
        if (state == STATE_SEARCHING_FOR_HCE) {
            this.updateReaderMode(STATE_PRESENTING_HCE_IN_FOREGROUND);
        }
    }
}