package com.kennethogjakob9000.wakeup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jakob on 22/09/15.
 */
public class UserUpdate implements ValueEventListener {

    private Firebase database  = null;
    private Map<String,Marker> usernameMarkerMap = null;
    private Map<String,User> userMap = null;
    private GoogleMap mMap = null;

    UserUpdate (Map<String, Marker> map, GoogleMap mMap, Firebase ref, Map<String, User> userMap) {
        this.usernameMarkerMap = map;
        this.userMap = userMap;
        this.mMap = mMap;
        this.database = ref;
    }

    @Override
    public void onDataChange (DataSnapshot dataSnapshot) {


        for (DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
            User user = postSnapShot.getValue(User.class);

            if (usernameMarkerMap.containsKey(user.getUsername())) {
                Marker m = usernameMarkerMap.get(user.getUsername());
                m.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
            } else {
                Marker m = mMap.addMarker( new MarkerOptions()
                    .position(new LatLng(user.getLatitude(), user.getLongitude()))
                    .title(user.getUsername()));
                usernameMarkerMap.put(user.getUsername(), m);
            }
        }
        /*
        Set<User> list = ((Map<User, Marker>) dataSnapshot.getValue()).keySet();
        for (User user: list) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(user.getLatitude(),user.getLongitude()))
                        .title(""));
            map.put(user,
                    marker);
        }
        */
    }

    @Override
    public void onCancelled (FirebaseError firebaseError) {
        System.out.println("Error on read: " + firebaseError);
    }
}
