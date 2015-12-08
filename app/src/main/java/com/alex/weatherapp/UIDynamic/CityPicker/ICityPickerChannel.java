package com.alex.weatherapp.UIDynamic.CityPicker;

/**
 * Created by Alex on 17.11.2015.
 */

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

/** Serves as connection between activity and fragment. But activity knows nothing about
 * handling user input, because it is up to a concrete ICityPicker implementation. So, activity
 * just hands this call to its ICityPicker, which also extends this interface.
 */
public interface ICityPickerChannel {
    void cityPicked(LocationData cityPicked);
}
