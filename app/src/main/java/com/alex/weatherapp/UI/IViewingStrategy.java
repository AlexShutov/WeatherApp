package com.alex.weatherapp.UI;

/**
 * Created by Alex on 07.10.2015.
 */

import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UI.PlacesViewer.IPlacesViewer;

/**
 * This entity is responsible for interaction of places list viewer and forecast viewer in
 * terms of laying them out properly. Strategy also handles selections.
 */

public interface IViewingStrategy extends IPlaceForecastViewer,
        IPlacesViewer.IPlaceSelectedCallback,
        IForecastViewer.IOtherPlaceButonCallback {
    void setHolder(IViewerAndDataHolder holder);
}
