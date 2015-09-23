package com.kennethogjakob9000.wakeup;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class StartMap extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ValueEventListener, LocationListener {

    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    private Marker lastLocationMarker;
    private LocationRequest mLocationRequest = null;

    private GoogleMap mMap; // Might be null if Google Play services API is not available.

    private String username;

    String databasePath = "https://kennethogjakob-wakeup.firebaseio.com";

    //private Firebase ref = null;

    //private Firebase userRef = null;

    private User user = null;

    private Map<String, Marker> userToMarker;

    private Map<String, User> usernameToUser = new HashMap<String, User>();

    private UserUpdate userUpdate = null;

    private int counter = 0;

    private boolean running = true;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private boolean mRequestingLocationUpdates;

    public StartMap () {
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        userToMarker = new HashMap<String, Marker>();

        String test = "hi";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_map);
        Intent intent = getIntent();
        username = intent.getStringExtra(LoginScreen.USERNAME);


        //Firebase.setAndroidContext(this);

        Firebase ref = new Firebase( databasePath );


        Firebase userRef = ref.child("users").child(username);

        user = new User(username, 0.0, 0.0, userRef);

        buildGoogleApiClient();

        setUpMapIfNeeded();

        userUpdate = new UserUpdate(mGoogleApiClient, user);

        ref.child("users").addValueEventListener(this);

        mRequestingLocationUpdates = false;
        //getLastLocation();

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPostResume () {
        super.onPostResume();
    }

    @Override
    protected void onResume () {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded () {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap () {
        mMap.setMyLocationEnabled(true);
        lastLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(0.0, 0.0))
                .title(""));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void getLastLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double latitude = 0.0, longitude = 0.0;
        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }

        String text = (mLastLocation == null) ? "No last position" : username;
        lastLocationMarker.setPosition(new LatLng(latitude,longitude));
        lastLocationMarker.setTitle(text);


        user.updateLocation(latitude, longitude);
    }

    @Override
    public void onConnected (Bundle bundle) {

        if (mLastLocation == null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }




    @Override
    public void onConnectionSuspended (int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChange (DataSnapshot dataSnapshot) {

        System.out.println(dataSnapshot.getChildrenCount() + " are a lot");

        for (DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
            System.out.println("Am I here?");
            User user = postSnapShot.getValue(User.class);

            if (userToMarker.containsKey(user.getUsername())) {
                Marker m = userToMarker.get(user.getUsername());
                m.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
            } else {
                Marker m = mMap.addMarker( new MarkerOptions()
                        .position(new LatLng(user.getLatitude(), user.getLongitude()))
                        .title(user.getUsername()));
                userToMarker.put(user.getUsername(), m);
            }

        }

    }


    @Override
    public void onCancelled (FirebaseError firebaseError) {

    }

    private void createLocationRequest () {
        mLocationRequest =  new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged (Location location) {
        mLastLocation = location;
        getLastLocation();
    }
}
