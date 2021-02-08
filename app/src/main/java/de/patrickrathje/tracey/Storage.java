package de.patrickrathje.tracey;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import de.patrickrathje.tracey.model.Group;

public class Storage extends Observable {

    private static Storage instance;

    public static synchronized Storage getStorage() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    List<Group> groups = new ArrayList<>();
    public List<Group> getGroups() {
        return new ArrayList<>(groups);
    }

    public int addGroup(Group group) {
        int id = groups.size();
        group.setId(id);

        groups.add(group);

        setChanged();

        // trigger notification
        notifyObservers(groups);

        return id;
    }
}
