package de.patrickrathje.tracey;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.icu.lang.UCharacter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import de.patrickrathje.tracey.model.Group;
import de.patrickrathje.tracey.ui.group_join.GroupJoinFragment;
import de.patrickrathje.tracey.utils.HexConverter;
import de.patrickrathje.tracey.utils.InvitationParser;

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



    private String scannedHexData = null;
    private String shareData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);


        // we load some dummy data!
        for(int i = 0; i < 20; i++) {
            Storage.getStorage().addGroup(new Group(new Date(), i % 2 == 0 ? "test" + String.valueOf(i) : ""));
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home,R.id.navigation_groups, R.id.navigation_settings).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter != null) {
            nfcAdapter.setNdefPushMessage(null, this); // Disable BEAM for this activity
        }

        // TODO: We should make this configurable
        startService(new Intent(this, ApduService.class));
    }


    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp();
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
                Log.i("Tracey", "Sending: " + HexConverter.ByteArrayToHexString(command));
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
                        Log.i("Tracey", "Received data 2 was not okay: " + HexConverter.ByteArrayToHexString(result));
                    }

                } else {
                    Log.i("Tracey", "Received data was not okay: " + HexConverter.ByteArrayToHexString(result));
                }
            } catch (IOException e) {
                Log.e("Tracey", "Error communicating with card: " + e.toString());
            }
        }

        // present HCE in the background
        this.updateReaderMode(STATE_PRESENTING_HCE_IN_BACKGROUND);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (scannedHexData != null) {
            // we scanned something! 
            Bundle bundle = new Bundle();
            System.out.println(scannedHexData);
            bundle.putString(GroupJoinFragment.ARG_HEX_DATA, scannedHexData);
            scannedHexData = null;
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.joinGroup, bundle);
        }
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


    public void createGroup(View view) {
        Group group = new Group(new Date(), "");
        int id = Storage.getStorage().addGroup(group);

        Bundle bundle = new Bundle();
        bundle.putInt("group_id", (int)id);
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.showGroupDetails, bundle);
    }

    public void shareGroupNFC(Group group) {

        shareData = InvitationParser.toHexInvitationString(group);
        startService((new Intent(this, ApduService.class)).setAction("setShareData").putExtra("shareData", shareData));

        // We start to search for HCE
        this.updateReaderMode(STATE_SEARCHING_FOR_HCE);

        final MainActivity self = this;
        // and we open a dialog

        ProgressDialog nDialog;
        nDialog = new ProgressDialog(this);
        nDialog.setMessage("Sending Group to other Tracey devices");
        nDialog.setIndeterminate(true);
        nDialog.setCancelable(true);
        nDialog.show();
        nDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                shareData = null;
                startService((new Intent(self, ApduService.class)).setAction("resetShareData"));
                self.updateReaderMode(STATE_PRESENTING_HCE_IN_BACKGROUND);
            }
        });
    }

    public void joinGroupNFC(View view) {
        // We start to search for HCE
        this.updateReaderMode(STATE_SEARCHING_FOR_HCE);

        shareData = null;
        startService((new Intent(this, ApduService.class)).setAction("resetShareData"));

        final MainActivity self = this;
        // and we open a dialog

        ProgressDialog nDialog;
        nDialog = new ProgressDialog(this);
        nDialog.setMessage("Searching for Tracey device");
        nDialog.setIndeterminate(true);
        nDialog.setCancelable(true);
        nDialog.show();
        nDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                self.updateReaderMode(STATE_PRESENTING_HCE_IN_BACKGROUND);
            }
        });
    }

    public void joinGroupQR(View view) {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan the Tracey QR Code");
        integrator.setBeepEnabled(false);
        integrator.setOrientationLocked(false);

        integrator.initiateScan();
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                scannedHexData = result.getContents();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}