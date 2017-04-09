package com.ove.termostat.model;

public class SensorRelay  {
    private String name;
    private float currentTemp;
    private int targetTemp;
    private int hysteresis;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(float currentTemp) {
        this.currentTemp = currentTemp;
    }

    public int getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(int targetTemp) {
        this.targetTemp = targetTemp;
    }

    public int getHysteresis() {
        return hysteresis;
    }

    public void setHysteresis(int hysteresis) {
        this.hysteresis = hysteresis;
    }

    public void setDefaults(int i) {
        name = "---";
        currentTemp = 0;
        targetTemp = 0;
        hysteresis = 0;
    }
}
