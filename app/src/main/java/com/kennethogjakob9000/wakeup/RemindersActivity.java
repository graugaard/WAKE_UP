package com.kennethogjakob9000.wakeup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RemindersActivity extends AppCompatActivity {

    ArrayList<String> remindUsers = null;
    Set<String> users = null;
    private String username = "";

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        remindUsers = getIntent().getStringArrayListExtra(StartMap.REMIND);
        username = getIntent().getStringExtra(LoginScreen.USERNAME);
    }

    protected void onResume() {
        super.onResume();
        remindUsers = getIntent().getStringArrayListExtra(StartMap.REMIND);
        users = new HashSet<String>();
        for(String n : remindUsers) {
            users.add(n);
        }

        username = getIntent().getStringExtra(LoginScreen.USERNAME);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminders, menu);
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

    public void remind(View view) {
        EditText edit = (EditText) findViewById(R.id.user_option);
        String text = edit.getText().toString().trim();
            users.add(text);
    }

    public void ignore(View view) {
        EditText edit = (EditText) findViewById(R.id.user_option);
        String text = edit.getText().toString().trim();
        users.remove(text);
    }

    public void returnToMap(View view) {
        Intent intent = new Intent(this, StartMap.class);
        ArrayList<String> list = new ArrayList<String>();

        for (String n : users) {
            list.add(n);
        }
        System.out.println("Set has size: " + users.size() + " and list has size: " + list.size());
        intent.putStringArrayListExtra(StartMap.REMIND, list);
        intent.putExtra(LoginScreen.USERNAME, username);
        startActivity(intent);
        finish();
    }
}
