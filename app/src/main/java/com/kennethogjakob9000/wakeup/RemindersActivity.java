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

import com.fasterxml.jackson.databind.deser.Deserializers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemindersActivity extends AppCompatActivity {

    ArrayList<userData> remindUsers = null;
    Set<String> users = null;
    private String username = "";
    ListView lview = null;
    public class userData  {
        String name = "";
        int dist = 5;
        userData(String n, int dist) {
            name = n;
            this.dist = dist;
        }
        userData() {
            ; // we already initialized name
        }
    }
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);


    }

    protected void onResume() {
        super.onResume();
        List<String>list = getIntent().getStringArrayListExtra(StartMap.REMIND);
        users = new HashSet<String>();
        remindUsers = new ArrayList<userData>();
        if (list != null) {
            for (String s : list) {
                users.add(s);
                userData u = new userData();
                u.name = s;
                remindUsers.add(u);
            }
        }


        username = getIntent().getStringExtra(LoginScreen.USERNAME);

        final Adapter adapter = new Adapter();
        ListView listview = (ListView)findViewById(R.id.listview);
        lview = listview;
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                                                userData data = adapter.getUserdata(position);
                                                Toast.makeText(RemindersActivity.this,
                                                        data.name + "\n" + data.dist,
                                                        Toast.LENGTH_LONG).show();
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
        EditText dist = (EditText) findViewById(R.id.distance);
        String text = edit.getText().toString().trim();
        String d = dist.getText().toString().trim();
        int distance = 0;
        if (!d.equals("")) {
            try {
                distance = Integer.parseInt(d);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Distance must be an int, setting to default 5 m",
                        Toast.LENGTH_SHORT)
                        .show();
                distance = 5;
            }
        }
        if (!users.contains(text)) {
            users.add(text);
            userData u = new userData(text,5);
            remindUsers.add(u);
            ((BaseAdapter)lview.getAdapter()).notifyDataSetChanged();
        }
        for (userData u : remindUsers) {
            if (u.name.equals(text)) {
                u.dist = distance;
                ((BaseAdapter)lview.getAdapter()).notifyDataSetChanged();
            }
        }
    }

    public void ignore(View view) {
        EditText edit = (EditText) findViewById(R.id.user_option);
        String text = edit.getText().toString().trim();
        users.remove(text);
    }

    public void returnToMap(View view) {
        Intent intent = new Intent(this, StartMap.class);
        ArrayList<String> list = new ArrayList<String>(remindUsers.size());
        ArrayList<Integer> dist = new ArrayList<>(remindUsers.size());
        for (userData u : remindUsers) {
            list.add(u.name);

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
            TextView dist = (TextView)convertView.findViewById(R.id.user_distance);
            userData userdata = data.get(position);
            text.setText(userdata.name);
            dist.setText(userdata.dist + " m");
            return convertView;
        }

        public userData getUserdata (int position) {
            return data.get(position);
        }
    }

    List<userData> getDataForListView() {

        return remindUsers;
    }

}
