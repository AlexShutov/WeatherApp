package com.alex.weatherapp.UIv2;

import android.util.Log;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.MapsFramework.Interfacing.IFeedbackInterface;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.Utils.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Alex on 25.11.2015.
 */
public class ViewingController implements IViewingController {
    public ViewingController(){
        mCityPicker = null;
        mForecastViewer = null;
        mCityPickerExternalFeedback = null;
        mForecasts = new TreeMap<>();
        mKnownPlaces = new TreeSet<>();
    }

    @Override
    public void assignPlacePicker(ICityPicker placePicker){
        mCityPicker = placePicker;
    }
    @Override
    public void assignForecastViewer(IForecastViewer forecastViewer) {
        mForecastViewer = forecastViewer;
    }

    @Override
    public void handleListOfPlaces(List<LocationData> placesToShow) {
        if (placesToShow.isEmpty()){
            Logger.w("Trying to show empty list of places, all places will be removed");
        } else {
            Logger.d("Showing list of places, total " + placesToShow.size() + " places");
        }
        mKnownPlaces.clear();
        mKnownPlaces.addAll(placesToShow);
        ArrayList<LocationData> t = new ArrayList<>();
        t.addAll(mKnownPlaces);
        mCityPicker.setCities(t);
    }

    @Override
    public void handleIncomingForecast(PlaceForecast forecast) {
        LocationData place = forecast.getPlace();
        boolean updatePlacePicker = false;
        if (!mKnownPlaces.contains(place)){
            mKnownPlaces.add(place);
            updatePlacePicker = true;
        } else {
            mForecasts.remove(place);
        }
        mForecasts.put(place, forecast);
        /** usually all places comes before forecasts, because all db requests are performed
         * sequentially by HandlerThread, but just in case, update list of all places (normally is
         * never called
         */
        if (updatePlacePicker){
            ArrayList<LocationData> places = new ArrayList<>();
            places.addAll(mKnownPlaces);
            mCityPicker.setCities(places);
        }
        /** Forecast for picked city has arrived, update forecast viewer */
        if (mCityPicker.getPickedCity().equals(place)){
            mForecastViewer.showForecast(forecast.getForecast());
        }
    }

    @Override
    public PlaceForecast getForecast(LocationData place) throws IllegalStateException,
            IllegalArgumentException {
        if (!mKnownPlaces.contains(place)){
            String msg = "requested place: " + place.getmPlaceName() +
                    " not found";
            Logger.w(msg);
            throw new IllegalArgumentException(msg);
        }
        if (!mForecasts.containsKey(place)){
            String msg = "There are no forecast for place: " + place.getmPlaceName() + " yet";
            Logger.w(msg);
            throw new IllegalStateException(msg);
        }
        PlaceForecast f = mForecasts.get(place);
        return f;
    }

    @Override
    public void setOnCityPickedFeedback(ICityPickedFeedback cityPickedFeedback) {

    }


    private ICityPicker mCityPicker;
    private IForecastViewer mForecastViewer;
    private ICityPickedFeedback mCityPickerExternalFeedback;

    private Set<LocationData> mKnownPlaces;
    private Map<LocationData, PlaceForecast> mForecasts;
}
