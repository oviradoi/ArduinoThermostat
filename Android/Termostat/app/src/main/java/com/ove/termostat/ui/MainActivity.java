package com.ove.termostat.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.ove.termostat.R;
import com.ove.termostat.model.GetInfoTask;

public class MainActivity extends AppCompatActivity implements GetInfoTask.InfoListener, ListView.OnItemClickListener {

    public static final String SHARED_PREFS_NAME = "TERMOSTAT_PREFS";
    public static final String IP_ADDRESS_PREF_KEY = "IP_PREF";
    public static final String IP_ADDRESS_PREF_DEFAULT = "192.168.2.145";

    public static final String PASSWORD_PREF_KEY = "PWD_PREF";
    public static final String PASSWORD_PREF_DEFAULT = "Test";

    private TermostatApp termostatApp;

    private SensorRelayAdapter adapter;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GetInfoTask getInfoTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        termostatApp = (TermostatApp)getApplicationContext();

        setContentView(R.layout.activity_main);

        adapter = new SensorRelayAdapter(this, R.layout.sensorrelay_listitem , termostatApp.sensorRelays);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        listView = (ListView)findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_termostat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void setInfo(String[] names, float[] currentTemps, int[] targetTemps, int[] hysteresis) {
        if(names != null) {
            for (int i = 0; i < names.length; i++) {
                termostatApp.sensorRelays[i].setName(names[i]);
            }
        }
        if(currentTemps != null) {
            for (int i = 0; i < currentTemps.length; i++) {
                termostatApp.sensorRelays[i].setCurrentTemp(currentTemps[i]);
            }
        }
        if(targetTemps != null) {
            for (int i = 0; i < targetTemps.length; i++) {
                termostatApp.sensorRelays[i].setTargetTemp(targetTemps[i]);
            }
        }
        if(hysteresis != null) {
            for (int i = 0; i < hysteresis.length; i++) {
                termostatApp.sensorRelays[i].setHysteresis(hysteresis[i]);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void hideProgress() {
        new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setIpAddress) {
            showSettings();
            return true;
        } else if (id == R.id.action_setPassword) {
            showPassword();
            return true;
        } else if (id == R.id.refresh) {
            swipeRefreshLayout.setRefreshing(true);
            refreshList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshList() {
        if (getInfoTask == null || getInfoTask.getStatus() != AsyncTask.Status.RUNNING) {
            getInfoTask = new GetInfoTask(this);
            SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            String url = prefs.getString(IP_ADDRESS_PREF_KEY, IP_ADDRESS_PREF_DEFAULT);
            String pwd = prefs.getString(MainActivity.PASSWORD_PREF_KEY, MainActivity.PASSWORD_PREF_DEFAULT);
            getInfoTask.execute(url, pwd);
        }
    }

    private void showPassword(){
        final SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String pwd = prefs.getString(PASSWORD_PREF_KEY, PASSWORD_PREF_DEFAULT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setPassword);
        builder.setMessage(R.string.setPassword);
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setText(pwd);
        builder.setView(et);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(PASSWORD_PREF_KEY, et.getText().toString());
                editor.apply();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    private void showSettings(){
        final SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String ip = prefs.getString(IP_ADDRESS_PREF_KEY, IP_ADDRESS_PREF_DEFAULT);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setIpAddress);
        builder.setMessage(R.string.ip_address);
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setText(ip);
        builder.setView(et);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(IP_ADDRESS_PREF_KEY, et.getText().toString());
                editor.apply();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(this, DetailsActivity.class);
        i.putExtra(DetailsActivity.EXTRA_POSITION, position);
        startActivity(i);
    }
}
