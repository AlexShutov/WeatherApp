package com.alex.weatherapp.UIv2;

import android.app.Activity;
import android.util.Log;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.Utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 26.11.2015.
 */
public class DummyViewingController implements IViewingController {

    class DummyCityPicker implements ICityPicker{
        @Override
        public void setActivity(Activity activity) {

        }

        @Override
        public void setCities(ArrayList<LocationData> places) {

        }

        @Override
        public void addCity(LocationData city) {

        }

        @Override
        public void removeCity(LocationData city) {

        }

        @Override
        public void removeCity(String placeName) {

        }

        @Override
        public void clear() {

        }

        @Override
        public boolean isHaving(LocationData place) {
            return false;
        }

        @Override
        public void pickCity(LocationData city) {

        }

        @Override
        public LocationData getPickedCity() throws IllegalStateException {
            return null;
        }

        @Override
        public void saveState() {

        }

        @Override
        public void saveState(LocationData pickedLocation) {

        }

        @Override
        public void restoreState() {

        }

        @Override
        public void refresh() {

        }

        @Override
        public void setFeedback(ICityPickedFeedback feedbackImpl) {

        }

        @Override
        public ICityPickedFeedback getFeedback() {
            return new ICityPickedFeedback() {
                @Override
                public void onCityPicked(LocationData pickedCity) {
                    DummyViewingController.this.logPickedCity(pickedCity);
                }
            };
        }

        @Override
        public void setChannel() {

        }

        @Override
        public void cityPicked(LocationData cityPicked) {

        }
    }

    public DummyViewingController(String name){
        mControllerName = name;
    }

    @Override
    public void assignPlacePicker(ICityPicker placePicker) {
        logMethod("assignPlacePicker(ICityPicker placePicker)");
    }

    @Override
    public void assignForecastViewer(IForecastViewer forecastViewer) {
        logMethod("assignForecastViewer(IForecastViewer forecastViewer)");
    }

    @Override
    public ICityPicker getAssignedPicker() throws IllegalStateException {
        logMethod("ICityPicker getAssignedPicker()");
        return new DummyCityPicker();
    }

    @Override
    public void handleListOfPlaces(List<LocationData> placesToShow) {
        logMethod("void handleListOfPlaces(List<LocationData> placesToShow)");
    }

    @Override
    public void handleIncomingForecast(PlaceForecast forecast) {
        logMethod("void handleIncomingForecast(PlaceForecast forecast) for "+
        forecast.getPlace().getmPlaceName());
    }

    @Override
    public void setOnCityPickedFeedback(ICityPickedFeedback cityPickedFeedback) {
        logMethod("void setOnCityPickedFeedback(ICityPickedFeedback cityPickedFeedback)");
    }

    @Override
    public PlaceForecast getForecast(LocationData place) throws IllegalStateException, IllegalArgumentException {
        logMethod("PlaceForecast getForecast(LocationData place)");
        return null;
    }

    @Override
    public List<LocationData> getKnownPlaces() {
        logMethod("List<LocationData> getKnownPlaces()");
        return null;
    }

    private void logMethod(String methodName){
        String msg = "Dummy viewing controller " + mControllerName + ". Called " + methodName;
        Logger.i(msg);
    }

    private void logPickedCity(LocationData city){
        Logger.i("Dummy viewing controller " + mControllerName +"City picked: " +
                city.getmPlaceName());
    }

    private String mControllerName;
}
