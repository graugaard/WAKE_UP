package com.kennethogjakob9000.wakeup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

import com.firebase.client.Firebase;

public class LoginScreen extends AppCompatActivity {
    public final static String USERNAME = "com.mycompany.myfirstapp.MESSAGE";
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        Firebase.setAndroidContext(this);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        Intent intent = new Intent(this,StartMap.class);
        EditText editText = (EditText) findViewById(R.id.username);
        String username = editText.getText().toString();
        intent.putExtra(USERNAME, username.trim());
        startActivity(intent);
    }
}
