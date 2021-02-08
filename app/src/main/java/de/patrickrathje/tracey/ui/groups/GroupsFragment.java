package de.patrickrathje.tracey.ui.groups;

import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.patrickrathje.tracey.R;

public class GroupsFragment extends Fragment {

    private GroupsViewModel GroupsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        detailFragment = new GroupDetailFragment();

// configure link
        Bundle bundle = new Bundle();
        bundle.putString("link", link);
        detailFragment.setArguments(bundle);

        GroupsViewModel =
                new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(GroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_groups, container, false);
        return root;
    }
}