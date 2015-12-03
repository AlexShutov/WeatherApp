package com.alex.weatherapp.UIv2;

import android.location.LocationManager;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUForecastData;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUndergroundGeolookupData;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.Utils.Logger;


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

    /**
     * We check whether picker has feedback interface set and if does, we use it as
     * external feedback for this controller
     * @param placePicker
     */
    @Override
    public void assignPlacePicker(ICityPicker placePicker){
        mCityPicker = placePicker;
        if (null == mCityPicker){
            return;
        }
        ICityPickedFeedback pickerFeedback = placePicker.getFeedback();
        if (null != pickerFeedback){
            setOnCityPickedFeedback(pickerFeedback);
        }
        placePicker.setFeedback(mPlacePickerAndForecastViewerBinder);
    }
    @Override
    public void assignForecastViewer(IForecastViewer forecastViewer) {
        mForecastViewer = forecastViewer;
    }

    @Override
    public ICityPicker getAssignedPicker() throws IllegalStateException {
        if (null == mCityPicker){
            String msg = "Caller are trying to return unassigned ICityPicker";
            Logger.w(msg);
            throw new IllegalStateException(msg);
        }
        return mCityPicker;
    }

    @Override
    public IForecastViewer getAssignedForecastViewer() throws IllegalStateException {
        if (null == mCityPicker){
            String msg = "Caller are trying to return unassigned IForecastViewer";
            Logger.w(msg);
            throw new IllegalStateException(msg);
        }
        return mForecastViewer;
    }

    @Override
    public void handleListOfPlaces(List<LocationData> placesToShow) {
        if (placesToShow.isEmpty()){
            Logger.w("Trying to show empty list of places, all places will be removed");
        } else {
            Logger.d("Showing list of places, total " + placesToShow.size() + " places");
        }
        //mForecasts.clear();
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

        LocationData currPicked = null;
        try {
            currPicked = mCityPicker.getPickedCity();
            if (null != currPicked && currPicked.equals(place)){
                mForecastViewer.showForecast(forecast.getForecast());
            }
        }catch (IllegalStateException ise){
            Logger.w("can't return a picked city, reason: " + ise.getMessage());
        }
        /** in the case when current picked place is unknown because ui isn't rendered yet,
         * request picker to pick that place again and viewer to show forecast for that place
         */

        if (null == currPicked){
            Logger.w("UI isn't rendered, acquiring place and forecast update anyway");
            mCityPicker.pickCity(place);
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
    public List<LocationData> getKnownPlaces() {
        List<LocationData> places = new ArrayList<>();
        places.addAll(mKnownPlaces);
        return places;
    }

    @Override
    public void clear() {
        mKnownPlaces.clear();
        mForecasts.clear();
        mCityPicker.clear();
        mForecastViewer.showForecast(new Forecast());
    }

    @Override
    public void addPlace(LocationData place) {

        mKnownPlaces.add(new LocationData(place));
        mCityPicker.addCity(new LocationData(place));
    }

    @Override
    public void setOnCityPickedFeedback(ICityPickedFeedback cityPickedFeedback) {
        mCityPickerExternalFeedback = cityPickedFeedback;
    }

    @Override
    public void saveState() {
        if (null == mCityPicker){
            Logger.e("Can't save picker's state, because it is null");
        }else {
            mCityPicker.saveState();
        }
    }

    @Override
    public void restoreState() {
        if (null == mCityPicker){
            Logger.e("Can't restore picker's state, because it is null");
        }else {
            mCityPicker.restoreState();
        }
    }

    /**
     * Here we accept place selected by user. Place picker will update itself, here we need
     * to show forecast for that place
     */
    private void processUserSelectingPlace(LocationData pickedPlace){
        PlaceForecast pf = null;
        try{
            pf = this.getForecast(pickedPlace);
            mForecastViewer.showForecast(pf.getForecast());
            mCityPicker.pickCity(pickedPlace);
        }catch (IllegalStateException ise){
        }catch (IllegalArgumentException iae){
        }
        if (null == pf){
            Forecast dummy = new Forecast();
            mForecastViewer.showForecast(dummy);
        }
        if (null != mCityPickerExternalFeedback){
            mCityPickerExternalFeedback.onCityPicked(pickedPlace);
        }

    }
    private ICityPickedFeedback mPlacePickerAndForecastViewerBinder =
            new ICityPickedFeedback() {
                @Override
                public void onCityPicked(LocationData pickedCity) {
                    processUserSelectingPlace(pickedCity);
                }
            };

    private ICityPickedFeedback mCityPickerExternalFeedback;

    private ICityPicker mCityPicker;
    private IForecastViewer mForecastViewer;

    private Set<LocationData> mKnownPlaces;
    private Map<LocationData, PlaceForecast> mForecasts;
}
