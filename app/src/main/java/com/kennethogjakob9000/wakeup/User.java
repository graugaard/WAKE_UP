package com.kennethogjakob9000.wakeup;

import com.firebase.client.Firebase;

/**
 * This class is meant to represent users of the application
 * Created by jakob on 22/09/15.
 **/
public class User {
    private String username = "";
    private double latitude = 0.0;
    private double longitude = 0.0;

    private Firebase userRef = null;

    public User( String username, double latitude, double longitude, Firebase userRef) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userRef = userRef;
    }

    public User( String username, Firebase userRef) {
        new User(username, 0.0, 0.0, userRef);
    }

    public void updateLocation(double longitude, double latitude) {
        this.latitude = latitude;
        this.longitude = longitude;

        if (userRef != null) {
            userRef.setValue(this);
        }

    }

    public String getUsername () {
        return username;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
