package de.patrickrathje.tracey.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Group {

    String text;
    Date dateCreated;
    Date dateAdded;
    Secret secret;
    Integer id = 0;
    
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


    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(getDateAdded());
    }
}
