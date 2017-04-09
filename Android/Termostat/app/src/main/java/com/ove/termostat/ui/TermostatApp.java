package com.ove.termostat.ui;

import android.app.Application;

import com.ove.termostat.model.SensorRelay;

public class TermostatApp extends Application {
    public SensorRelay[] sensorRelays;

    public TermostatApp() {
        this.sensorRelays = new SensorRelay[4];
        for(int i=0;i<4;i++){
            sensorRelays[i] = new SensorRelay();
            sensorRelays[i].setDefaults(i);
        }
    }
}
