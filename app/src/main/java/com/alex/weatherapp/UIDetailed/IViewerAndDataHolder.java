package com.alex.weatherapp.UIDetailed;

/**
 * Created by Alex on 07.10.2015.
 */

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIDetailed.PlacesViewer.IPlacesViewer;

import java.util.Map;

/**
 * Describes the 'hub', which holds references to forecast and places viewers. It implements
 * strategy and decorator patterns. First is used for being able to support different screen
 * orientations (that's why it extends IForecastViewer) - when layout has two frame views
 * side by side, it must be configured for using two-pane strategy, when there is only one frame,
 * it uses single-pane strategy, which switches  between views and activates 'select different
 * place' button in forecast viewer. Here is no explicit method 'setViewingStrategy(...)',
 * because layout type is pre-defined by screen orientation and activity recreates holder every
 * screen rotation and requests data from db. Holder also contains a map of all received forecasts
 * (Map<LocationData, Forecast>). The data's arrival order is irrelevant- it either has forecast
 * for a place, or not- place viewer watches handling state (name, localCache, online).
 */
public interface IViewerAndDataHolder extends IPlaceForecastViewer {
    /**
     * clears container of received data and show empty views.
     */
    void reset();
    /** Component accessors */
    IPlacesViewer getPlaceViewer();
    IForecastViewer getForecastViewer();
    ILinkToHolderActivity getLink();
    Map<LocationData, Forecast> getData();
    LocationData getCurrPlace();
    void setCurrPlace(LocationData place);
}
