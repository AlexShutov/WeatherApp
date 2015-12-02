package com.alex.weatherapp.UIv2.CityPicker;

/**
 * Created by Alex on 17.11.2015.
 */

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.Utils.Logger;

import java.util.ArrayList;

/**
 * maintains some basic stuff- stores all places, UI context an so on.
 */
public class CityPicker implements ICityPicker {
    private static String CITY_PICKER_PREFERENCES_NAME = "city_picker_saved state";
    private static String LAST_PICKED_CITY_NAME = "last_picked_city_name";
    private static String LAST_PICKED_LATITUDE = "last_picked_location_latitude";
    private static String LAST_PICKED_LONGITUDE = "last_picked_location_longitude";

    public CityPicker(int frameID){
        init();
        mFrameID = frameID;
    }
    private void init(){
        mActivity = null;
        mFeedback = null;
        mCities = new ArrayList<>();
    }

    @Override
    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void setCities(ArrayList<LocationData> places) {
        if (places.isEmpty()){
            Logger.d("Received empty place list");
            mCities = places;
            Fragment f = createFragment(null);
            return;
        }
        mCities = places;
        refresh();
    }

    /**
     * Two places are the same if they has the same coordinates
     * @param city
     */
    @Override
    public void addCity(LocationData city) {
        if (mCities.contains(city)){
            return;
        }
        mCities.add(city);
        refresh();
    }

    @Override
    public void removeCity(LocationData city) {
        boolean had = mCities.remove(city);
        mCities.remove(city);
        if (had){
            CityPickerFragment f = getFragment();
            if ( null != f) {
                f.setNewPlacesData(mCities);
            }
        }
    }

    @Override
    public void removeCity(String placeName) {
        LocationData place = null;
        for (LocationData c : mCities){
            if (c.getmPlaceName().equals(placeName)) {
                place = c;
                break;
            }
        }
        if (null != place){
            removeCity(place);
        }
    }

    @Override
    public void clear() {
        mCities.clear();
        createFragment(null);
    }

    @Override
    public boolean isHaving(LocationData place) {
        return mCities.contains(place);
    }

    @Override
    public void pickCity(LocationData city) {
        if (!isHaving(city)){
            Logger.e("Error: trying to pick not existing city");
            return;
        }
        CityPickerFragment pickerUI = getFragment();
        if (null == pickerUI){
            pickerUI = createFragment(city);
        }else {
            if (!mCities.isEmpty())
            pickerUI.setSelected(city);
        }
    }

    @Override
    public LocationData getPickedCity() throws IllegalStateException {
        CityPickerFragment f = getFragment();
        if (null == f){
            throw new IllegalStateException("Lack of picked fragment, perhaps it isn't created yet");
        }
        return f.getPicked();
    }

    @Override
    public void saveState() {
        LocationData selected = null;
        try {
            selected = getPickedCity();
        }catch (IllegalStateException ise){
            Logger.w("can't save last picked value, because there are no picker fragment");
            return;
        }
        saveState(selected);
    }

    @Override
    public void saveState(LocationData pickedLocation) {
        if (null == pickedLocation){
            return;
        }
        LocationData selected = pickedLocation;
        SharedPreferences prefs = mActivity.getSharedPreferences(CITY_PICKER_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        Logger.d("Saving last picked city");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_PICKED_CITY_NAME, selected.getmPlaceName());
        String lat = String.valueOf(selected.getLat());
        String lon = String.valueOf(selected.getLon());
        editor.putString(LAST_PICKED_LATITUDE, lat);
        editor.putString(LAST_PICKED_LONGITUDE, lon);
        editor.commit();
    }

    @Override
    public void restoreState() {

        SharedPreferences prefs = mActivity.getSharedPreferences(CITY_PICKER_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        String placeName = prefs.getString(LAST_PICKED_CITY_NAME, "");
        String latitude = prefs.getString(LAST_PICKED_LATITUDE, "0");
        String longitude = prefs.getString(LAST_PICKED_LONGITUDE, "0");
        double lat = Double.valueOf(latitude);
        double lon = Double.valueOf(longitude);
        LocationData place = new LocationData(lat, lon, placeName);
        if (!mCities.contains(place)){
            Logger.w("Current list of cities doesn't have previously selected value: "
            + place.getmPlaceName());
            return;
        }
        if (!mCities.isEmpty()) pickCity(place);
    }

    private CityPickerFragment getFragment(){
        FragmentManager fm = mActivity.getFragmentManager();
        Fragment currFragment = fm.findFragmentByTag(CityPickerFragment.CITY_PICKER_FRAGMENT);
        CityPickerFragment cpf = null;
        if (null == currFragment || !(currFragment instanceof CityPickerFragment)){
            return null;
        }else {
            cpf = (CityPickerFragment) currFragment;
        }
        return cpf;
    }

    private CityPickerFragment createFragment(LocationData selected){
        FragmentManager fm = mActivity.getFragmentManager();
        CityPickerFragment cpf = getFragment();
        if (null != selected) {
            cpf = CityPickerFragment.newInstance(mCities);
        }else {
            cpf = CityPickerFragment.newInstance(mCities, selected);
        }
        fm.beginTransaction().replace(mFrameID, cpf, CityPickerFragment.CITY_PICKER_FRAGMENT)
                    .commit();

        return cpf;
    }

    @Override
    public void refresh() {
        CityPickerFragment fragment = getFragment();
        if (null == fragment){
            fragment = createFragment(null);
        }else {
            fragment.setNewPlacesData(mCities);
        }
    }

    @Override
    public void setFeedback(ICityPickedFeedback feedbackImpl) {
        mFeedback = feedbackImpl;
    }
    @Override
    public ICityPickedFeedback getFeedback() {
        return mFeedback;
    }

    @Override
    public void setChannel() {
    }

    @Override
    public void cityPicked(LocationData cityPicked) {
        if (null != mFeedback){
            if (null != cityPicked) {
                Logger.d("City picked: " + cityPicked.getmPlaceName());
            }else {
                Logger.d("No city is picked");
            }
            mFeedback.onCityPicked(cityPicked);
        }
    }

    public int getFrameID(){ return mFrameID;}

    private int mFrameID;
    protected Activity mActivity;
    protected ArrayList<LocationData> mCities;
    private ICityPickedFeedback mFeedback;

}
