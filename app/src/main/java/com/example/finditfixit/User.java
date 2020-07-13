package com.example.finditfixit;

import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String fault;
    private double latitude, longitude;


    public User() {
    }

    public User(String fault, double latitude, double longitude, String name) {
        this.fault = fault;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFault() {
        return fault;
    }

    public void setFault(String fault) {
        this.fault = fault;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
