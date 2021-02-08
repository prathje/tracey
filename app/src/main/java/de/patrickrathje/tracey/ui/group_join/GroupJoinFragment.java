package de.patrickrathje.tracey.ui.group_join;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Objects;

import de.patrickrathje.tracey.R;
import de.patrickrathje.tracey.Storage;
import de.patrickrathje.tracey.model.Group;
import de.patrickrathje.tracey.utils.InvitationParser;

public class GroupJoinFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_HEX_DATA = "hex_data";

    /**
     * The parsed group information
     */
    private Group group;

    public GroupJoinFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        System.out.println("######################");
        System.out.println(getArguments().getString(ARG_HEX_DATA));

        if (getArguments().containsKey(ARG_HEX_DATA)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            String hexData = getArguments().getString(ARG_HEX_DATA);

            group = InvitationParser.parseHexInvitationString(hexData);

            if (group == null) {
                System.out.println("Error while parsing group");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_join, container, false);

        // Show the dummy content as text in a TextView.
        if (group != null) {
            Activity activity = this.getActivity();

            ((AppCompatActivity) activity).getSupportActionBar().setTitle(group.toString());

            ((TextView) rootView.findViewById(R.id.textView)).setText("Join the group? " + group.getText());

            GroupJoinFragment self = this;

            rootView.findViewById(R.id.btnJoinGroup).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Save group and navigate to group details
                    Storage.getStorage().addGroup(group);
                    Bundle bundle = new Bundle();
                    bundle.putInt("group_id", group.getId());

                    group = null; // reset the group!
                    Navigation.findNavController(Objects.requireNonNull(self.getActivity()), R.id.nav_host_fragment).navigate(R.id.showGroupDetails, bundle);
                }
            });
        }

        return rootView;
    }
}