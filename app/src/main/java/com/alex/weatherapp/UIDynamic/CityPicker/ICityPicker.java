package com.alex.weatherapp.UIDynamic.CityPicker;

import android.app.Activity;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

import java.util.ArrayList;

/**
 * Created by Alex on 17.11.2015.
 */
public interface ICityPicker extends ICityPickerChannel {
    void setActivity(Activity activity);
    void setCities(ArrayList<LocationData> places);
    void addCity(LocationData city);

    /** Selection callback is fired after altering data. We may want to change data without
     * notifying the rest of a programm about it. For that call this method before callin
     * add(..) and set(..) methods.
     */
    void disableNextSelectionCallbackFiring(int n, LocationData selectAfterModification);
    void removeCity(LocationData city);
    void removeCity(String placeName);
    void clear();
    /** shows whether current cities list has a particular city */
    boolean isHaving(LocationData place);
    void pickCity(LocationData city);
    LocationData getPickedCity() throws IllegalStateException;
    /** Context will be set in subclass, call in onPause and onCreate */
    void saveState();
    void saveState(LocationData pickedLocation);
    void restoreState();

    /** forces implementation to update its ui due to
     * new data arrival (is done automatically in other methods
     */
    void refresh();

    /** Set feedback implementation, notifies the rest of a program
     * when user would have picked some city
     */
    void setFeedback(ICityPickedFeedback feedbackImpl);
    ICityPickedFeedback getFeedback();

    /**
     * Set connection with actual ui (interface, implemented
     * by activity for receiving fragment feedback), call this
     * method in activity, passing activity instance
     */
    void setChannel();
}
