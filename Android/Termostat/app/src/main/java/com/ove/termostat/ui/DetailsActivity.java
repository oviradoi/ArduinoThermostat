package com.ove.termostat.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.ove.termostat.R;
import com.ove.termostat.model.GetInfoTask;
import com.ove.termostat.model.SensorRelay;
import com.ove.termostat.model.SetInfoTask;

import java.util.Locale;

public class DetailsActivity extends AppCompatActivity implements GetInfoTask.InfoListener, SetInfoTask.SetInfoListener {

    public static String EXTRA_POSITION = "Position";

    private GetInfoTask getInfoTask;

    private int sensorPosition;
    private SensorRelay sensorRelay;

    private TermostatApp termostatApp;

    private TextView txtCurrentTemp;
    private TextView txtTargetTemp;
    private TextView txtHysteresis;

    private EditText editTarget;
    private EditText editHyst;
    private Button setTarget;
    private Button setHyst;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SetInfoTask setInfoTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        termostatApp = (TermostatApp) getApplicationContext();

        setContentView(R.layout.activity_details);

        txtCurrentTemp = (TextView) findViewById(R.id.tvCurrentTemp);
        txtTargetTemp = (TextView) findViewById(R.id.tvTargetTemp);
        txtHysteresis = (TextView) findViewById(R.id.tvHyst);
        editTarget = (EditText) findViewById(R.id.editTarget);
        editHyst = (EditText) findViewById(R.id.editHyst);
        setTarget = (Button) findViewById(R.id.setTarget);
        setHyst = (Button) findViewById(R.id.setHyst);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        setTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTarget();
            }
        });

        setHyst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHyst();
            }
        });

        editTarget.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshButtonsEnable();
            }
        });

        editTarget.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    setTarget();
                    return true;
                }
                return false;
            }
        });

        editHyst.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshButtonsEnable();
            }
        });
        editHyst.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    setHyst();
                    return true;
                }
                return false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }
        });

        Intent intent = getIntent();
        sensorPosition = intent.getIntExtra(EXTRA_POSITION, 0);
        sensorRelay = termostatApp.sensorRelays[sensorPosition];

        refreshUI();
        refreshButtonsEnable();
    }

    private void setTarget(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        String targetText = editTarget.getText().toString();
        if(!TextUtils.isEmpty(targetText)) {
            if (setInfoTask == null || setInfoTask.getStatus() != AsyncTask.Status.RUNNING) {
                setInfoTask = new SetInfoTask(this);
                SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                String url = prefs.getString(MainActivity.IP_ADDRESS_PREF_KEY, MainActivity.IP_ADDRESS_PREF_DEFAULT);
                String pwd = prefs.getString(MainActivity.PASSWORD_PREF_KEY, MainActivity.PASSWORD_PREF_DEFAULT);
                setInfoTask.execute(SetInfoTask.SET_TARGET, url, (sensorPosition + 1) + "", targetText, pwd);
            }
        }
    }

    private void setHyst(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        String hystText = editHyst.getText().toString();
        if(!TextUtils.isEmpty(hystText)) {
            if (setInfoTask == null || setInfoTask.getStatus() != AsyncTask.Status.RUNNING) {
                setInfoTask = new SetInfoTask(this);
                SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                String url = prefs.getString(MainActivity.IP_ADDRESS_PREF_KEY, MainActivity.IP_ADDRESS_PREF_DEFAULT);
                String pwd = prefs.getString(MainActivity.PASSWORD_PREF_KEY, MainActivity.PASSWORD_PREF_DEFAULT);
                setInfoTask.execute(SetInfoTask.SET_HYST, url, (sensorPosition + 1) + "", hystText, pwd);
            }
        }
    }

    private void refreshUI(){
        setTitle(sensorRelay.getName());
        txtCurrentTemp.setText(String.format(Locale.getDefault(), "%.1f\u00B0C", sensorRelay.getCurrentTemp()));
        txtHysteresis.setText(String.format(Locale.getDefault(), "%d\u00B0C", sensorRelay.getHysteresis()));
        txtTargetTemp.setText(String.format(Locale.getDefault(), "%d\u00B0C", sensorRelay.getTargetTemp()));
    }

    private void refreshButtonsEnable(){
        setTarget.setEnabled(!TextUtils.isEmpty(editTarget.getText().toString()));
        setHyst.setEnabled(!TextUtils.isEmpty(editHyst.getText().toString()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            swipeRefreshLayout.setRefreshing(true);
            Refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Refresh(){
        if (getInfoTask == null || getInfoTask.getStatus() != AsyncTask.Status.RUNNING) {
            getInfoTask = new GetInfoTask(this);
            SharedPreferences prefs = getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            String url = prefs.getString(MainActivity.IP_ADDRESS_PREF_KEY, MainActivity.IP_ADDRESS_PREF_DEFAULT);
            String pwd = prefs.getString(MainActivity.PASSWORD_PREF_KEY, MainActivity.PASSWORD_PREF_DEFAULT);
            getInfoTask.execute(url, pwd);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void setInfo(String[] names, float[] currentTemps, int[] targetTemps, int[] hysteresis) {
        if (names != null) {
            termostatApp.sensorRelays[sensorPosition].setName(names[sensorPosition]);
        }
        if (currentTemps != null) {
            termostatApp.sensorRelays[sensorPosition].setCurrentTemp(currentTemps[sensorPosition]);
        }
        if (targetTemps != null) {
            termostatApp.sensorRelays[sensorPosition].setTargetTemp(targetTemps[sensorPosition]);
        }
        if (hysteresis != null) {
            termostatApp.sensorRelays[sensorPosition].setHysteresis(hysteresis[sensorPosition]);
        }
        refreshUI();
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
    public void SetInfoComplete(Boolean success) {
        swipeRefreshLayout.setRefreshing(true);
        Refresh();
    }
}
