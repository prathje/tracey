package de.patrickrathje.tracey;

import java.util.ArrayList;
import java.util.List;

import de.patrickrathje.tracey.model.Group;

public class Storage {

    private static Storage instance;

    public static synchronized Storage getStorage() {
        if (instance == null) {
            instance = new Storage();
        }
        return instance;
    }

    List<Group> groups = new ArrayList<>();
    public List<Group> getGroups() {
        return groups;
    }

    public void addGroup(Group group) {
        group.setSaved(true);
        groups.add(group);
    }

    public void removeGroup(Group group) {
        group.setSaved(false);
        groups.remove(group);
    }
}
