package de.patrickrathje.tracey.ui.groups;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.List;

import de.patrickrathje.tracey.R;
import de.patrickrathje.tracey.Storage;
import de.patrickrathje.tracey.model.Group;

public class GroupsFragment extends ListFragment {

    private GroupsViewModel GroupsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_groups, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        List<Group> groups = Storage.getStorage().getGroups();

        int size = groups.size();
        String[] values = new String[size];

        for(int i = 0; i < size; i++) {
            Group g = groups.get(i);

            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            values[i] = sdf.format(g.getDateAdded());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Bundle bundle = new Bundle();
        bundle.putInt("group_id", (int)id);
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.showGroupDetails, bundle);

        /*
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setAction("show_group");
        //TODO: The list index is not the best id ;)
        intent.putExtra("group_id",id);
        startActivity(intent);*/
    }
}