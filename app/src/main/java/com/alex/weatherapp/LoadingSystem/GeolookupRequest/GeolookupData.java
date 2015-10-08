package com.alex.weatherapp.LoadingSystem.GeolookupRequest;

/**
 * Created by Alex on 06.09.2015.
 */

/**
 * tag class
 */
public class GeolookupData {
    public GeolookupData(){
        mLat = 0;
        mLon = 0;
    }
    public GeolookupData(double lat, double lon) {
        mLat = lat;
        mLon = lon;
    }

    public double getLat() { return  mLat; }
    public double getLon() { return  mLon; }
    public void setLat(double lat){ mLat = lat;}
    public void setLon(double lon){ mLon = lon;}

    private double mLat;
    private double mLon;
}
