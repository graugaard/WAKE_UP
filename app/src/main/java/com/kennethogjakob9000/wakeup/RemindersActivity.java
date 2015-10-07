package com.kennethogjakob9000.wakeup;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemindersActivity extends AppCompatActivity {

    ArrayList<String> remindUsers = null;
    Set<String> users = null;
    private String username = "";
    public class userData  {
        String name = "";
    }
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);


    }

    protected void onResume() {
        super.onResume();
        remindUsers = getIntent().getStringArrayListExtra(StartMap.REMIND);
        users = new HashSet<String>();
        for(String n : remindUsers) {
            users.add(n);
        }

        username = getIntent().getStringExtra(LoginScreen.USERNAME);

        remindUsers = getIntent().getStringArrayListExtra(StartMap.REMIND);
        username = getIntent().getStringExtra(LoginScreen.USERNAME);

        final Adapter adapter = new Adapter();
        ListView listview = (ListView)findViewById(R.id.listview);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                userData data = adapter.getUserdata(position);
                Toast.makeText(RemindersActivity.this,data.name,Toast.LENGTH_LONG).show();
            }
        }
        );

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

    public class Adapter extends BaseAdapter {

        List<userData> data = getDataForListView();

        @Override
        public int getCount () {
            return data.size();
        }

        @Override
        public userData getItem (int position) {
            return data.get(position);
        }

        @Override
        public long getItemId (int position) {
            return position;
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listitem,parent,false);
            }
            TextView text = (TextView)convertView.findViewById(R.id.name_of_user);
            userData userdata = data.get(position);
            text.setText(userdata.name);
            return convertView;
        }

        public userData getUserdata (int position) {
            return data.get(position);
        }
    }

    List<userData> getDataForListView() {
        List<userData> list = new ArrayList<userData>();
        for (String s: remindUsers) {
            userData userdata = new userData();
            userdata.name = s;
            list.add(userdata);
        }
        return list;
    }

}
