package de.patrickrathje.tracey.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;

import de.patrickrathje.tracey.R;

public class ContactsFragment extends Fragment {

    private ContactsViewModel contactsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        contactsViewModel =
                new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(ContactsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);
        return root;
    }
}