package de.patrickrathje.tracey.ui.groups;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.patrickrathje.tracey.R;
import de.patrickrathje.tracey.model.Group;

public class GroupListFragment extends Fragment {

    private OnGroupSelectedListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rsslist_overview,
                container, false);
        Button button = (Button) view.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetail(new Group());
            }
        });
        return view;
    }

    public interface OnGroupSelectedListener {
        public void onGroupSelected(Group link);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGroupSelectedListener) {
            listener = (OnGroupSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implemenet MyListFragment.OnGroupSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // may also be triggered from the Activity
    public void updateDetail(Group group) {
        listener.onGroupSelected(group);
    }
}
