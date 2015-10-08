package com.alex.weatherapp.UI;

/**
 * Created by Alex on 07.10.2015.
 */


import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;

import java.util.List;

/** This interface represents interaction of activity and widgets, showing forecasts for a places.
 *  By widgets I assume IForecastViewer and IForecastViewer
 *  When everything is set, use it as response to new forecast arrival.
 */

public interface IPlaceForecastViewer extends ILinkToHolderActivity {


    /**
     * Shows list of places, regardless of currently active viewer. If in single-frame mode,
     * list of places will be brought to front. Is usually called right after Activity creation,
     * when database returns list of saved places. It will clear all saved data and will initialize
     * them with default values. It is ok, because ForecastViewer will have state 'only names' for
     * each of the place so it skips click events.
     * @param places list of places to display
     */
    void showPlaces(List<LocationData> places);

    /**
     * In two-pane mode updates list of places, in single-pane mode id doesn't bring
     * list of places to the front, but if PlacesView is alredy in front, it updates its state
     * @param f
     */
    void showPlaceForecast(PlaceForecast f);
    void showPlacesForecasts(List<PlaceForecast> forecasts);

}
