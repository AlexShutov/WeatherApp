package com.alex.weatherapp.MVP;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;

import java.util.List;

/**
 * Created by Alex on 30.09.2015.
 */
public interface IViewContract {

    void handleListOfSavedPlaces(List<LocationData> locations);
    void showPlacesForecasts(List<PlaceForecast> forecasts);
    void showPlaceForecast(PlaceForecast forecast);

    /**
     * callback method for getStandaloneForecast(), see IPresenterContract
     * @param forecast
     */
    void showStandalonePlaceForecast(PlaceForecast forecast);
    void onNewPlaceIsAddedToPlaceRegistry(LocationData placeInfo);
    void onAllPlacesRemoved();

    void showOnlineForecast(PlaceForecast forecast);
    void showGoogleGeolookup(LocationData placeLoc, String placeName);
}
