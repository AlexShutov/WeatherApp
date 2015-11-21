package com.alex.weatherapp.UIv2.CityPicker;

import android.app.Activity;
import android.content.Context;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 17.11.2015.
 */
public interface ICityPicker extends ICityPickerChannel {
    void setActivity(Activity activity);
    void setCities(ArrayList<LocationData> places);
    void addCity(LocationData city);
    void removeCity(LocationData city);
    void removeCity(String placeName);
    void clear();
    /** shows whether current cities list has a particular city */
    boolean isHaving(LocationData place);
    void pickCity(LocationData city);
    LocationData getPickedCity() throws IllegalStateException;
    /** Context will be set in subclass, call in onPause and onCreate */
    void saveState();
    void restoreState();

    /** forces implementation to update its ui due to
     * new data arrival (is done automatically in other methods
     */
    void refresh();

    /** Set feedback implementation, notifies the rest of a program
     * when user would have picked some city
     */
    void setFeedback(ICityPickedFeedback feedbackImpl);

    /**
     * Set connection with actual ui (interface, implemented
     * by activity for receiving fragment feedback), call this
     * method in activity, passing activity instance
     */
    void setChannel();

}
