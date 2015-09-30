package com.kennethogjakob9000.wakeup;

import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

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
    private WifiManager wifiMgr = null;
    private String networkname = "";
    private String networkAddr = "";

    public User( String username, double latitude, double longitude, Firebase userRef,
                 WifiManager wifiMgr) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userRef = userRef;
        this.wifiMgr = wifiMgr;
    }

    public User() {
        // use default values
    }

    public void updateLocation(double latidude, double longitude) {
        this.latitude = latidude;

        this.longitude = longitude;

        if (userRef != null) {
            userRef.setValue(this);
        }

        updateNetworkInfo();

    }

    private void updateNetworkInfo () {
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        if (wifiInfo != null ) {
            networkname = wifiInfo.getSSID();
        } else {
            networkname = "Can't find WIFI";
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

    public String getNetworkname () {
        return networkname;
    }

    public void updateLocation (Location location) {
        updateLocation(location.getLatitude(), location.getLongitude());
    }
}
