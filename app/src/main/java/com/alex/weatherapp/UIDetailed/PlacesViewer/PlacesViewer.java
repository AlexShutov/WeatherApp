package com.alex.weatherapp.UIDetailed.PlacesViewer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.Utils.Logger;
import com.alex.weatherapp.WeatherApplication;

import java.util.List;

/**
 * Created by Alex on 06.10.2015.
 */
public class PlacesViewer implements IPlacesViewer {
    private static String CITY_PICKER_PREFERENCES_NAME = "city_picker_saved state";
    private static String LAST_PICKED_CITY_NAME = "last_picked_city_name";
    private static String LAST_PICKED_LATITUDE = "last_picked_location_latitude";
    private static String LAST_PICKED_LONGITUDE = "last_picked_location_longitude";

    public PlacesViewer(Activity activity){
        mActivity = activity;
        init();
    }

    private void init(){
        mUIFrameResourceID = 0;
        mIsSilentModeActive = false;
        mRegistry = new RegistryOfPlaces();
        mRegistry.reset();
    }

    @Override
    public void reset() {
        mRegistry.reset();
    }

    /** Inherited from IForecastViewer */
    @Override
    public void setFrameResourceID(int frameResourceID) {
        mUIFrameResourceID = frameResourceID;
        mExternalEventListener = null;
    }

    @Override
    public void setSelectedCallback(IPlaceSelectedCallback placeSelectedCallback) {
        mExternalEventListener = placeSelectedCallback;
    }

    @Override
    public void turnOnSilentMode(boolean isOn) {
        mIsSilentModeActive = isOn;
    }

    @Override
    public void showPlaceList(List<LocationData> placesToShow) {
        if (placesToShow == null) return;
        if (placesToShow.isEmpty()){
            showEmptyFragment();
            return;
        }
        mRegistry.reset();
        mRegistry.addPlaces(placesToShow);
        invalidate();
    }




    @Override
    public void processIncomingResponse(LocationData placeToUpdate, boolean addNotExisting) {
        try {
            mRegistry.processPlaceRequestThrow(placeToUpdate);
        } catch (IllegalArgumentException e){
            Log.d("Place Viewer", "Illegal place has arrived");
            if (addNotExisting){
                mRegistry.addPlace(placeToUpdate);
                mRegistry.processPlaceRequest(placeToUpdate);
            }
        }
        invalidate();
    }

    /** The methods, inherited from fragment callback. These method are called by fragment and
     * redirected in here by holding activity
     */
    @Override
    public void onPlaceSelected(int viewPosition) {
        if (mExternalEventListener != null){
            LocationData clickedPlace = null;
            /** make sure we have data for this place, for that check its state */
            try {
                clickedPlace = mRegistry.getPlaceByEncounter(viewPosition);
                /** make sure we have data for this place, for that check its state */
                PlaceUpdateState placeState = mRegistry.getPlaceState(clickedPlace);
                if (placeState == PlaceUpdateState.NameOnly){
                    return;
                }
            }catch (IndexOutOfBoundsException e){
                Log.d("ForecastViewer", "Error, index out of bound because of unknown reason");
                return;
            }
            mExternalEventListener.onPlaceSelected(clickedPlace);
        }
    }

    @Override
    public void onEmptyViewClicked() {
        if (mExternalEventListener != null){
            mExternalEventListener.onNoPlaceUpdateRequested();
        }
    }

    private void showEmptyFragment(){
        if (mIsSilentModeActive) {
            return;
        }
        FragmentManager fm = mActivity.getFragmentManager();
        NoPlacesFoundFragment f = new NoPlacesFoundFragment();
        fm.beginTransaction().replace(mUIFrameResourceID, f).commit();
    }

    private void createAndPutFragment(){
        PlaceNameFragment viewFragment = PlaceNameFragment.newIntance(mRegistry);
        FragmentManager fm = mActivity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(mUIFrameResourceID, viewFragment, PlaceNameFragment.TAG_FRAGMENT_KIND);
        ft.addToBackStack(WeatherApplication.BACK_STACK_TAG);
        ft.commit();
    }

    @Override
    public void invalidate(){
        if (mIsSilentModeActive){
            return;
        }
        FragmentManager fm = mActivity.getFragmentManager();
        Fragment f = fm.findFragmentById(mUIFrameResourceID);
        if (f == null || !(f instanceof PlaceNameFragment)){
            createAndPutFragment();
            return;
        }
        PlaceNameFragment fPlace = (PlaceNameFragment) f;
        try {
            fPlace.updateData(mRegistry.getListOfPlaces(), mRegistry.getPlacesRequestStates());
        }
        catch (NullPointerException e){
            Log.d("PlacesViewer", "NullPointerException");
        }
    }

    @Override
    public void removePlace(LocationData place) {
        if (!mRegistry.removePlace(place)){
            return;
        }
        invalidate();
    }

    @Override
    public int getPlaceListPostion(LocationData place) throws IllegalArgumentException {
        return mRegistry.getPlaceEncounterNo(place);
    }

    @Override
    public LocationData getPlaceByPosition(int position) {
        return mRegistry.getPlaceByEncounter(position);
    }

    @Override
    public void saveState(LocationData pickedLocation) {
        if (null == pickedLocation){
            return;
        }
        LocationData selected = pickedLocation;
        SharedPreferences prefs = mActivity.getApplicationContext()
                .getSharedPreferences(CITY_PICKER_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        Logger.d("Saving last picked city");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_PICKED_CITY_NAME, selected.getPlaceName());
        String lat = String.valueOf(selected.getLat());
        String lon = String.valueOf(selected.getLon());
        editor.putString(LAST_PICKED_LATITUDE, lat);
        editor.putString(LAST_PICKED_LONGITUDE, lon);
        editor.apply();
    }

    @Override
    public LocationData restoreState() {
        SharedPreferences prefs = mActivity.getApplicationContext()
                .getSharedPreferences(CITY_PICKER_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        String placeName = prefs.getString(LAST_PICKED_CITY_NAME, "");
        String latitude = prefs.getString(LAST_PICKED_LATITUDE, "0");
        String longitude = prefs.getString(LAST_PICKED_LONGITUDE, "0");
        double lat = Double.valueOf(latitude);
        double lon = Double.valueOf(longitude);
        LocationData place = new LocationData(lat, lon, placeName);
        return place;
    }

    private IRegistryOfPlaces mRegistry;
    private int mUIFrameResourceID;
    private IPlaceSelectedCallback mExternalEventListener;
    private Activity mActivity;
    private boolean mIsSilentModeActive;

}
