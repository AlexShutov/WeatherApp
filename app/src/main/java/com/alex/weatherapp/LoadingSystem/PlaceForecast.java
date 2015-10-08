package com.alex.weatherapp.LoadingSystem;

import android.util.Log;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

/**
 * Created by Alex on 23.09.2015.
 */

/**
 * Container for place and forecast for thas place
 */
public class PlaceForecast {
    public PlaceForecast(LocationData place, Forecast forecast) {
        if(place == null) {
            place = new LocationData(0, 0);
            Log.d("WARNING", "NULL PLACE REFERENCE, DEFAULT VALUE IS USED");
        }
        if (forecast == null){
            Log.d("WARNING", "NULL FORECAST REFERENCE");
        }
        setPlace(place);
        setForecast(forecast);
    }
    public LocationData getPlace(){ return mPlace;}
    public void setPlace(LocationData place){ mPlace = place;}
    public Forecast getForecast() { return mForecast;}
    public void setForecast(Forecast forecast){ mForecast = forecast;}

    private LocationData mPlace;
    private Forecast mForecast;
}
