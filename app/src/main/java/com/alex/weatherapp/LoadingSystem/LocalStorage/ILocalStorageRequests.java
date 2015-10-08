package com.alex.weatherapp.LoadingSystem.LocalStorage;

/**
 * Created by Alex on 09.09.2015.
 */

import android.util.Pair;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupData;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationResponse;

import java.util.Date;
import java.util.List;

/**
 * Methods prototypes, making sense in employed schema
 */

public interface ILocalStorageRequests {

    /**
     * delete all table records
     */
    void dropForecastTable();

    /**
     * Adds forecast for a given place
     * @param placeCoordinates
     * @param forecast
     * @return true on success, false on failure
     */
    public boolean addNewForecast(GeolookupData placeCoordinates, Forecast forecast);

    /**
     *
     * @param coord geo coordinates
     * @return forecast for a given place
     */
    Forecast getRecordsByCoordinates(GeolookupData coord);

    /**
     * @return aquires list of coordinates for the distinct places
     */
    List<GeolookupData> getDistinctCoordinates();

    /**
     * @return Fetch entire database, coordinates is stored apart from forecasts
     */
    Pair<List<Forecast>,List<GeolookupData> > getAllRecordsForAllLocations();

    /**
     * @param date Day we need get forecasts for
     * @return day forecasts and coordinates for a distinct places. may have multiple
     * occurences if database hasn't been cleaned before insertion
     */
    Pair<List<Forecast.DayForecast>,List<GeolookupData>> getForecastForADay(Date date);

    /**
     * Remove expired forecast
     * @param tresholdDate expiration date
     */
    void deleteObsoleteForecasts(Date tresholdDate);


    void deletePlaceForecast(GeolookupData placeCoordinates);


    /**
     * Here comes place requests
     * @param place
     * @return
     */
    public boolean addNewPlace(LocationData place);
    public void getOnePlaceByCoordinates(LocationData place);
    public LocationResponse getAllPlaces();
    public void deletePlaceByCoord(LocationData place);
    public void dropPlacesTable();

}
