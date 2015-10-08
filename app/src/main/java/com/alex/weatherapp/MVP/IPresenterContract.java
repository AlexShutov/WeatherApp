package com.alex.weatherapp.MVP;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

/**
 * Created by Alex on 30.09.2015.
 */
public interface IPresenterContract {

    void addNewPlace(LocationData placeInfo);
    void removeAllPlaces();

    void acquireForecastsForAllPlaces();
    void getListOfSavedPlaces();
    void acquirePlaceForecast(LocationData placeCoords);

    /**
     * Use this method to get a forecast for one particular place. This request may have coordinates
     * of previously cached forecast, but it will retrieved without requesting data for other
     * places
     * @param placeCoord
     */
    void getStandAlonePlaceForecast(LocationData placeCoord);

    void getForecastOnlineNoCache(LocationData placeCoords);
    void getGoogleGeolookup(LocationData placeCoords);

    void setForceNetworkUpdate(boolean forceUpdate);
}
