package de.patrickrathje.tracey.ui.groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.patrickrathje.tracey.R;
import de.patrickrathje.tracey.Storage;
import de.patrickrathje.tracey.model.Group;

public class GroupsFragment extends ListFragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_groups, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Group> groups = Storage.getStorage().getGroups();
        Collections.reverse(groups);
        ArrayAdapter<Group> adapter = new ArrayAdapter<Group>(getActivity(), android.R.layout.simple_list_item_1, groups);
        setListAdapter(adapter);


        Storage.getStorage().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                List<Group> groups = Storage.getStorage().getGroups();
                Collections.reverse(groups);
                adapter.clear();
                adapter.addAll(groups);
                adapter.notifyDataSetChanged();
                //List<Group> groups = Storage.getStorage().getGroups();
                //adapter.clear();
                //adapter.addAll(groups);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Bundle bundle = new Bundle();
        bundle.putInt("group_id", ((Group)getListAdapter().getItem((int)id)).getId());
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.showGroupDetails, bundle);

        /*
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setAction("show_group");
        //TODO: The list index is not the best id ;)
        intent.putExtra("group_id",id);
        startActivity(intent);*/
    }
}