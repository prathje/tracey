package de.patrickrathje.tracey.ui.group_details;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;

import de.patrickrathje.tracey.R;
import de.patrickrathje.tracey.Storage;
import de.patrickrathje.tracey.model.Group;

public class GroupDetailsFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "group_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Group group;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            group = Storage.getStorage().getGroups().get(getArguments().getInt(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_details, container, false);

        // Show the dummy content as text in a TextView.
        if (group != null) {
            Activity activity = this.getActivity();
            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            ((AppCompatActivity) activity).getSupportActionBar().setTitle(sdf.format(group.getDateAdded()));

            ((TextView) rootView.findViewById(R.id.textView)).setText(group.getText());
        }

        return rootView;
    }
}