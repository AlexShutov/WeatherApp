package com.alex.weatherapp.LoadingSystem;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

import java.util.List;

/**
 * Created by Alex on 23.09.2015.
 */
public interface ILoadingFacade{

    interface IEmptyCallback {
        void onCompletion();
    }
    interface IOnLocationsLoadedCallback {
        void onCompletion(List<LocationData> locations);
    }

    /**
     * Helper class, gets all forecasts for all places. Can call callback on every new forecast and
     * the one on completion
     */
    interface IAllForecastsRetriever extends IOnLocationsLoadedCallback, IOnLocationForecast {
        void begin();
        void setEachForecastCallback(IOnLocationForecast callback);
        void setFinalCallback(IOnPlaceForecastsLoaded callback);
    }
    IAllForecastsRetriever getDataRetriever();

    void addNewPlace(double lat, double lon, String name, IEmptyCallback callback);
    void getAllLocations(IOnLocationsLoadedCallback callback);
    void removeAllPlcaes(IEmptyCallback callback);
    void getLocationName(double latitude, double longitude, ICallback callback);
    interface IOnLocationForecast {
        void onResult(LocationData place, Forecast forecast);
    }
    void getForecastForLocation(LocationData place, IOnLocationForecast callback);
    void getForecastForLocationOnlineNoCache(LocationData place, IOnLocationForecast callback);

    /**
     * Sometimes we might need to be able to update the data no matter what
     * (every N hours). So, call this method with true in the scheduled service, and when
     * all data is retrived, clear it and terminate the service.
     * @param alwaysUpdate
     */
    void setAlwaysUpdate(boolean alwaysUpdate);

    /* It is used by inner class during retrieval of forecasts of all places  */
    interface IOnPlaceForecastsLoaded {
        void getPlaceForecasts(List<PlaceForecast> placeForecasts);
    };
}
