package com.alex.weatherapp.UIDynamic.CityPicker;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

/**
 * Created by Alex on 17.11.2015.
 */
public interface ICityPickedFeedback {
    void onCityPicked(LocationData pickedCity);
}
