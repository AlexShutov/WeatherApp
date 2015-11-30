package com.alex.weatherapp.UIv2;

import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;

/**
 * Created by Alex on 26.11.2015.
 */
public interface IMapViewer extends ICityPicker, IForecastViewer, ICityPickedFeedback {
    void setViewingController(IViewingController viewingController);
}
