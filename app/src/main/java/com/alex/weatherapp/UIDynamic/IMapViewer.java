package com.alex.weatherapp.UIDynamic;

import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIDynamic.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIDynamic.CityPicker.ICityPicker;
import com.alex.weatherapp.UIDynamic.FreePlaceGeoMonitor.IFreePlaceMonitor;

/**
 * Created by Alex on 26.11.2015.
 */
public interface IMapViewer extends ICityPicker, IForecastViewer, ICityPickedFeedback {
    void setViewingController(IViewingController viewingController);
    void setFreePlaceMonitor(IFreePlaceMonitor monitor);
}
