package com.kennethogjakob9000.wakeup;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


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
import java.util.*;


public class StartMap extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ValueEventListener, LocationListener {

    protected GoogleApiClient mGoogleApiClient;

    public final String IGNORE = "ignore";
    public static final String REMIND = "remind";

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

    private User ourUser = null;

    private Map<String, Marker> userToMarker;

    private Map<String, User> usernameToUser = new HashMap<String, User>();

    private UserUpdate userUpdate = null;

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";


    protected static final String TAG = "location-updates-sample";

    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

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

    private Menu mainMenu = null;

    Set<String> onSameNetwork = null;

    ArrayList<String> remind = null;

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        remind = new ArrayList<String>();

        userToMarker = new HashMap<String, Marker>();

        onSameNetwork = new HashSet<String>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_map);
        Intent intent = getIntent();
        username = intent.getStringExtra(LoginScreen.USERNAME);

        Firebase ref = new Firebase( databasePath );

        WifiManager wifiMgr = (WifiManager)
                getSystemService(Context.WIFI_SERVICE);

        Firebase userRef = ref.child("users").child(username);

        ourUser = new User(username, 0.0, 0.0, userRef, wifiMgr);

        onSameNetwork.add(username);

        buildGoogleApiClient();

        setUpMapIfNeeded();

        userUpdate = new UserUpdate(mGoogleApiClient, ourUser);

        ref.child("users").addValueEventListener(this);

        mRequestingLocationUpdates = true;

        updateValuesFromBundle(savedInstanceState);

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
    protected void onResume () {
        super.onResume();
        setUpMapIfNeeded();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        ArrayList<String> remindusers = getIntent().getStringArrayListExtra(REMIND);
        if (remindusers != null) {
            remind = new ArrayList<>();
            remind = remindusers;
            System.out.println("Got something of length: " + remindusers.size());
            System.out.println("Got: " + remind);
        } else
            System.out.println("Didn't get anything");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(this, UserRemind.class);
        UserRemind reminder = new UserRemind();
        intent.putExtra(LoginScreen.USERNAME,username);
        intent.putStringArrayListExtra(REMIND, remind);
        startService(intent);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the ourUser to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A ourUser can return to this FragmentActivity after following the prompt and correctly
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
        createLocationRequest();
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


        ourUser.updateLocation(latitude, longitude);
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

        for (DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
            User user = postSnapShot.getValue(User.class);

            if (userToMarker.containsKey(user.getUsername())) {
                Marker m = userToMarker.get(user.getUsername());
                m.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
                if (user.getNetworkname().equals( ourUser.getNetworkname())
                        && user.getUsername().equals(ourUser.getUsername())) {
                    m.setSnippet("Test");
                }
                userToMarker.put(user.getUsername(), m);

                if (user.getNetworkname().equals(ourUser.getNetworkname()) &&
                        !onSameNetwork.contains(user.getUsername())) {
                    // remind of user on the network if he is.
                    if (remind.contains(user.getUsername().trim())) {
                        toast(user.getUsername() + " logged onto same wifi");
                        System.out.println("Username trimmed: " + user.getUsername().trim());
                        System.out.println("remind: " + remind);
                    }
                    onSameNetwork.add(user.getUsername());
                }
                // leaf network, so remove user from those on our network
                else if (!user.getNetworkname().equals(ourUser.getNetworkname()) &&
                        onSameNetwork.contains(user.getUsername())) {
                    onSameNetwork.remove(user.getUsername());
                }
            } else {
                Marker m = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(user.getLatitude(), user.getLongitude()))
                        .title(user.getUsername()));
                userToMarker.put(user.getUsername(), m);
            }

        }

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.

            getLastLocation();
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
        MenuItem item = null;
        if (mainMenu != null) {
            item = mainMenu.getItem(0);
        }
        if (item != null) {
            item.setTitle("WIFI : " + ourUser.getNetworkname());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_start_map, menu);
        mainMenu = menu;
        return true;
    }

    public void toast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, msg, duration).show();
    }

    public void gotoReminders(MenuItem item) {
        //EditText edit = (EditText) findViewById(R.id.user_option)
        Intent intent = new Intent(this, RemindersActivity.class);
        intent.putStringArrayListExtra(REMIND, remind);
        intent.putExtra(LoginScreen.USERNAME, username);
        startActivity(intent);
    }
}
