package com.kennethogjakob9000.wakeup;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;

public class UserRemind extends Service implements
        ValueEventListener {
    String databasePath = "https://kennethogjakob-wakeup.firebaseio.com";
    String username = "";
    WifiManager wifiMgr = null;
    User ourUser = null;

    Set<String> onSameNetwork = null;
    List<String> remind = null;

    public UserRemind () {

    }

    @Override
    public void onCreate(){
        HandlerThread thread = new HandlerThread("Reminder",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        onSameNetwork = new HashSet<>();
        Firebase ref = new Firebase( databasePath );
        Firebase userRef = ref.child("users").child(username);
        ourUser = new User(username, 0.0, 0.0, userRef, wifiMgr);
    }

    @Override
    public int onStartCommand(Intent intent, int flags,int startId) {
        Toast.makeText(this,"Listening for users.",Toast.LENGTH_SHORT).show();
        Firebase ref = new Firebase( databasePath );

        username = intent.getStringExtra(LoginScreen.USERNAME);
        wifiMgr = (WifiManager)
                getSystemService(Context.WIFI_SERVICE);
        remind = intent.getStringArrayListExtra(StartMap.REMIND);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }

    @Override
    public void onDataChange (DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapShot: dataSnapshot.getChildren()) {
            User user = postSnapShot.getValue(User.class);

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
        }
    }

    public void toast(String msg) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, msg, duration).show();
    }

    @Override
    public void onCancelled (FirebaseError firebaseError) {

    }
}
