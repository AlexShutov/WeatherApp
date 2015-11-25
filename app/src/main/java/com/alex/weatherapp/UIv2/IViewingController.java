package com.alex.weatherapp.UIv2;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;

import java.util.List;

/**
 * Created by Alex on 25.11.2015.
 */

/**
 * Describes the entity, responsible for processing incoming list of places and then
 * showing selected forecast. Place piceker and forecast viewer must already be initialized
 * before assign.
 */
public interface IViewingController {
    void assignPlacePicker(ICityPicker placePicker);
    void assignForecastViewer(IForecastViewer forecastViewer);

    void handleListOfPlaces(List<LocationData> placesToShow);

    /**
     * all forecast is being passed right after loader knows it (cached and then online, if
     * necessary)
     * @param forecast
     */
    void handleIncomingForecast(PlaceForecast forecast);

    /**
     * The other controllers might want to know that user selected another place (say, the map)
     * @param cityPickedFeedback
     */
    void setOnCityPickedFeedback(ICityPickedFeedback cityPickedFeedback);

    /**
     * Get forecast for a known place.
     * @param place
     * @return
     * @throws IllegalStateException is thrown when we're not received forecast yet, but we know
     * that place is on a list of places or list of places is empty (no places at all)
     * @throws IllegalArgumentException is thrown if list of places doesn't ave such a place.
     */
    PlaceForecast getForecast(LocationData place) throws IllegalStateException, IllegalArgumentException;
}
