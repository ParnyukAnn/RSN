package com.aparnyuk.rsn.model;

public class Sim {
    private String simName;
    private String deviceName;

    public Sim() {
    }

    public Sim(String simName, String deviceName) {
        this.simName = simName;
        this.deviceName = deviceName;
    }

    public String getSimName() {
        return simName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setSimName(String simName) {
        this.simName = simName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

}
