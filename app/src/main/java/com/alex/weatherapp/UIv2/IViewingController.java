package com.alex.weatherapp.UIv2;

import android.content.Context;

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

    /**
     * Different constructs, built upon this controller might want to use interface
     * for picking a place. One way is in allowing IViewingController implement it. But this
     * picker's stuff is 'too fat', so that's more easy to just return ICity picker instance.
     * Notice that this IViewingController decorates ICityPicker for intercepting user feedback.
     * After processing it (feedback) e.g. showing a forecast, external feedback gets fired.
     * @return
     * @throws IllegalStateException
     */
    ICityPicker getAssignedPicker() throws IllegalStateException;
    IForecastViewer getAssignedForecastViewer() throws IllegalStateException;
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

    void saveState();
    void restoreState();
    void clear();

    void addPlace(LocationData place);

    /**
     * Get forecast for a known place.
     * @param place
     * @return
     * @throws IllegalStateException is thrown when we're not received forecast yet, but we know
     * that place is on a list of places or list of places is empty (no places at all)
     * @throws IllegalArgumentException is thrown if list of places doesn't ave such a place.
     */
    PlaceForecast getForecast(LocationData place) throws IllegalStateException, IllegalArgumentException;
    List<LocationData> getKnownPlaces();
}
