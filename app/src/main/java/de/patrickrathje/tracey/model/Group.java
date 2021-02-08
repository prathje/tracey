package de.patrickrathje.tracey.model;

import java.util.Date;

public class Group {

    String text;
    Date dateCreated;
    Date dateAdded;
    Secret secret;
    boolean saved = false;



    public Group(Date dateCreated, String text, Secret secret) {
        this.dateCreated = dateCreated;
        this.dateAdded = new Date();
        this.text = text;
        this.secret = secret;
    }

    public Group(Date dateCreated, String text) {
        this.dateCreated = dateCreated;
        this.dateAdded = new Date();
        this.text = text;
        this.secret = new Secret();
    }

    public String getText() {
        return text;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Secret getSecret() {
        return secret;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
