package com.alex.weatherapp.LoadingSystem.ForecastRequest;

/**
 * Created by Alex on 06.09.2015.
 */

import com.alex.weatherapp.LoadingSystem.RequestAbstract;

/**
 * Geolookup has latitude and longitude, as a Geolookup request,
 * but its different, so it doesn't derive GeolookupRequest
 */
public class ForecastRequest implements RequestAbstract {

    public enum ForecastType {
        Forecast_3Days,
        Forecast_10Days
    };

    public ForecastRequest(double lat, double lon) {
        mLat = lat;
        mLon = lon;
        mForecastType = ForecastType.Forecast_3Days;
        mProcessOnlineNoCache = false;
    }

    public void setmForecastType(ForecastType type) { mForecastType = type;}
    public double getLat() { return  mLat; }
    public double getLon() { return  mLon; }
    public ForecastType getmForecastType(){ return  mForecastType;}
    private double mLat;
    private double mLon;
    private ForecastType mForecastType;

    public void setOnlineNoCache(boolean onc){
        mProcessOnlineNoCache = onc;
    }
    public boolean getOnlineNoCache(){
        return mProcessOnlineNoCache;
    }
    private boolean mProcessOnlineNoCache;
}
