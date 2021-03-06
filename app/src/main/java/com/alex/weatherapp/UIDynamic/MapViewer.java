package com.alex.weatherapp.UIDynamic;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.IFeedbackShapes;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ISysShapesDisplay;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeData;
import com.alex.weatherapp.R;
import com.alex.weatherapp.UIDynamic.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIDynamic.FreePlaceGeoMonitor.IFreePlaceMonitor;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Alex on 26.11.2015.
 */
public class MapViewer implements IMapViewer {
    private static final int CONSTANT_WEATHER_RADII = 20000;

    final String sAreaNamePrefix = "Area for: ";

    /** We need to be able to remove saved place, one way to do that is ask user about it
     * when he or she selects the same marker again (not area around marker, just marker,
     * because area might be tapped aidntly
     */
    interface IOnEditCallback {
        void handleRepeatingSelection(LocationData place);
        void handleFreePlace(LocationData place);
        void handleSelectionOfSavedPlace(LocationData selectedPlace);
    }
    public MapViewer(){
    }

    /**
     * All references are null by default, those are cleared here for not to to forget
     */
    private void init(){
        mMapIFace = null;
        /** gonna be rewritten anyway */
        mActivity = null;
        mPickedCity = null;
        mPickingFeedback = null;
        mViewingController = null;
        mCenterOfLastSelectedArea = null;
        /** repetitive marker clicks */
        mLastChoisenSavedPlace = null;
        mSelectedFreePlace = null;
        mEditCallback = null;
        mFreePlaceMonitor = null;
    }
    public void setMapInterface(ISysShapesDisplay mapInterface){
        mMapIFace = mapInterface;
        mMapIFace.setFeedbackInterface(mMapFeedback);
    }
    @Override
    public void setViewingController(IViewingController viewingController) {
        mViewingController = viewingController;
    }

    @Override
    public void setFreePlaceMonitor(IFreePlaceMonitor monitor) {
        if (null == monitor){
            Logger.w("Monitor reference inside MapViewer is null," +
                    " free place monitoring is now unavailible");
        }
        mFreePlaceMonitor = monitor;
    }

    /**
     * Inherited from IForecastViewer
     * @param pickedCity
     */
    @Override
    public void onCityPicked(LocationData pickedCity) {
        pickCity(pickedCity);
    }

    /** Inheited from ICityPicker */
    @Override
    public void setActivity(Activity activity) {
        mActivity = activity;
    }
    @Override
    public void setCities(ArrayList<LocationData> places) {
        Logger.i("MapViewer.setcities() is called");
        if (null == mConstantWeatherAreas) {
            mConstantWeatherAreas = new TreeMap<>();
        }else{
            mConstantWeatherAreas.clear();
        }
        mMapIFace.removeAllShapes();
        mMapIFace.removeInfoMarkers();
        for (LocationData place : places){
            addCity(place);
        }
    }
    @Override
    public void addCity(LocationData place) {
        PlaceData markerData = new PlaceData(place);
        markerData.setMarkerType(PlaceData.MarkerType.Standard);
        mMapIFace.addInfoMarker(markerData);
        LatLng center = new LatLng(place.getLat(), place.getLon());
        CircularRegionData weatherArea = new CircularRegionData(center, CONSTANT_WEATHER_RADII);
        weatherArea.setShapeName(sAreaNamePrefix + place.getPlaceName());
        mConstantWeatherAreas.put(place, weatherArea);
        mMapIFace.addCircularArea(weatherArea);

    }
    @Override
    public void removeCity(LocationData city) {

    }
    @Override
    public void removeCity(String placeName) {

    }
    @Override
    public void clear() {
        setViewingController(null);
        ArrayList<LocationData> areas = new ArrayList<>();
        areas.addAll(mConstantWeatherAreas.keySet());
        for (LocationData l : areas){
            mMapIFace.removeShape(sAreaNamePrefix + l.getPlaceName());
            PlaceData markerData = new PlaceData(l);
            markerData.setMarkerType(PlaceData.MarkerType.Standard);
            mMapIFace.removeInfoMarekr(markerData);
            mConstantWeatherAreas.remove(l);
        }
    }
    @Override
    public boolean isHaving(LocationData place) {
        return false;
    }
    @Override
    public void pickCity(LocationData city) {
       // ((UIDynamicActivity)mActivity).showPopup("Place is picked: " + city.getPlaceName());
        mPickedCity = city;
        mLastChoisenSavedPlace = city;
        if (null != mEditCallback) {
            mEditCallback.handleSelectionOfSavedPlace(city);
        }
        Logger.i("MapViewer.pickCity(" + city.getPlaceName() + ");");
        mMapIFace.moveAndZoomCamera(city, 9);
    }
    @Override
    public LocationData getPickedCity() throws IllegalStateException {
        return mPickedCity;
    }

    /**
     * The next three methods manages mapViewet's state and is used when MapViewer being
     * considered ICityPicker. MapViewer's state coincides with the state of other cityPicker,
     * so we don't use them for now. All saved places and forecasts is fetched from database and
     * then displayed. ICityPiker (Spinner version) saves current state and restores it, alongside
     * forcing MapViewer to draw places and focus on the selected one.
     */
    @Override
    public void saveState() {
        Logger.d("MapViewer.saveState() is called");
        if (null != mSelectedFreePlace){
            savePlaceToPrefs(mSelectedFreePlace, "SelectedFreePlace");
        }
    }
    @Override
    public void saveState(LocationData pickedLocation) {
    }
    @Override
    public void restoreState() {
        Logger.d("MapViewer.restoreState() is called");
        LocationData freePlace = restorePlaceFromPrefs("SelectedFreePlace");
        if (null == freePlace){
            mSelectedFreePlace = null;
        }else {
            mMapIFace.mimicTap(freePlace);
        }
    }
    private void savePlaceToPrefs(LocationData place, String placeName){
        SharedPreferences prefs = mActivity.getPreferences(Context.MODE_PRIVATE);
        String fieldName = placeName + "_name";
        String fieldLat = placeName + "_lat";
        String fieldLon = placeName + "_lon";
        if (null == place){
            prefs.edit().remove(fieldName).remove(fieldLat).remove(fieldLon).apply();
            return;
        }
        prefs.edit().putString(fieldName, place.getPlaceName())
                .putFloat(fieldLat, (float) place.getLat())
                .putFloat(fieldLon, (float)place.getLon()).apply();
    }
    private LocationData restorePlaceFromPrefs(String placeName){
        SharedPreferences prefs = mActivity.getPreferences(Context.MODE_PRIVATE);
        String fieldName = placeName + "_name";
        String fieldLat = placeName + "_lat";
        String fieldLon = placeName + "_lon";
        if (!prefs.contains(fieldName)){
            return null;
        }
        String name = prefs.getString(fieldName, "");
        double lat = (double) prefs.getFloat(fieldLat, 0);
        double lon = (double) prefs.getFloat(fieldLon, 0);
        return new LocationData(lat, lon, name);
    }
    @Override
    public void refresh() {
    }

    @Override
    public void setFeedback(ICityPickedFeedback feedbackImpl) {
        mPickingFeedback = feedbackImpl;
    }
    @Override
    public ICityPickedFeedback getFeedback() {
        return this;
    }
    @Override
    public void setChannel() {
    }

    @Override
    public void cityPicked(LocationData cityPicked) {
    }

    private IFeedbackShapes mMapFeedback = new IFeedbackShapes() {
        @Override
        public void onCircularRegionSelected(CircularRegionData data) {
            List<LocationData> places = new ArrayList<>();
            places.addAll(mConstantWeatherAreas.keySet());
            LatLng t = data.getCenter();
            LocationData center = new LocationData(t.latitude, t.longitude);
            LocationData areaAround = null;
            for (LocationData p : places) {
                center.setmPlaceName(p.getPlaceName());
                if (center.equals(p)) {
                    areaAround = p;
                    break;
                }
            }
            mCenterOfLastSelectedArea = areaAround;
            if (null != mFreePlaceMonitor){
                mFreePlaceMonitor.onSavedPlaceSelected();
            }
            if (null != areaAround) {
                    mPickingFeedback.onCityPicked(areaAround);
            }
        }
        @Override
        public void onRectRegionSelected(RectRegionData data) {
        }
        @Override
        public void onNothingSelected() {
        }
        /**
         * Framework firstly notifies about shape selection, so we can use it for defining,
         * whether new place were selected, or not.
         * @param place
         */
        @Override
        public void onNewPlacePinned(LocationData place) {
            /** user must tap on saved place twice, tapping any other area clears last tapped
             * saved place. So, marker, related to volatile place clears last tapped place
             */
            mLastChoisenSavedPlace = null;
            boolean isNew;
            if (null == mCenterOfLastSelectedArea){
                isNew = true;
            }else {
                float[] dist = new float[1];
                LocationData c = mCenterOfLastSelectedArea;
                Location.distanceBetween(c.getLat(), c.getLon(), place.getLat(), place.getLon(), dist);
                isNew = (dist[0] >= CONSTANT_WEATHER_RADII);
            }
            if (isNew) {
                mSelectedFreePlace = place;
                String lat = mActivity.getString(R.string.ids_pn_latitude_short);
                String lon = mActivity.getString(R.string.ids_pn_longitude_short);
                String placeName =
                        String.format(lat + ": %.4f, " + lon + ": %.4f", place.getLat(), place.getLon());
                place.setmPlaceName(placeName);
                String msg = place.getPlaceName();
                Logger.i(msg);
                /** notify other app's part throught callbacks if there any */
                if (null != mFreePlaceMonitor){
                    mFreePlaceMonitor.acceptNewFreePlace(place);
                }
                if (null != mEditCallback) {
                    mEditCallback.handleFreePlace(place);
                }
                if (null != mActivity) {
                    Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
        /** user may now observe any place on map, but this methods returns last saved place
         * being being picked
         * @return
         */
        LocationData getLastSelectedSavedPlace(){
            return mLastChoisenSavedPlace;
        }
        @Override
        public void onInfoMarkerClick(PlaceData infoMarker) {
            if (null != mPickingFeedback){
                LocationData place = infoMarker.getLocation();
                checkWhetherPlaceIsPickedAgainAndProcessIfItIs(place);
            } else {
                Logger.e("Place picker feedback ICityPickedFeedback isn't set");
            }
        }
        @Override
        public void showServiceMessage(String msg) {
            Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
        }
    };
    public void setEditCallback(IOnEditCallback cb){
        mEditCallback = cb;
    }
    /** Map may have another markers, for example, weather stations locations, that's why we need
     * a way for distinguishing them. For that MapViewer holds a link to its ViewingController,
     * which, in turn, knows about all saved places. We look through them for defining whether
     * clicked marker represent saved place
     * @param place
     * @return
     */
    private boolean checkWhetherClickedPlaceIsSavedOne(LocationData place){
        boolean isSavedPlace = false;
        if (mViewingController != null){
            List<LocationData> savedPlaces = mViewingController.getKnownPlaces();
            if (savedPlaces.contains(place)){
                return true;
            }
        }
        return false;
    }

    private void checkWhetherPlaceIsPickedAgainAndProcessIfItIs(LocationData place){
        boolean isSavedPlace = checkWhetherClickedPlaceIsSavedOne(place);
        if (isSavedPlace) {
            Logger.d("Marker for saved place were clicked");
            if (null != mLastChoisenSavedPlace &&
                    mLastChoisenSavedPlace.equals(place)){
                Logger.d("The same place being clicked repeatedly: " +
                        place.getPlaceName());
                if (null != mEditCallback){
                    mEditCallback.handleRepeatingSelection(place);
                }
            }
            if (null != mFreePlaceMonitor){
                /** saved place were clicked, notify free place monitor */
                mFreePlaceMonitor.onSavedPlaceSelected();
            }
            mPickingFeedback.onCityPicked(place);
            mSelectedFreePlace = null;
            if (null != mEditCallback) {
                mEditCallback.handleSelectionOfSavedPlace(place);
            }
            mLastChoisenSavedPlace = place;
        }
    }

    /** Inherited from IForecastViewer */
    /** figure out the place and move camera to show it with zoom
     * @param dayForecastorecast
     * Those methods are useless here
     * */
    @Override
    public void showDayForecast(Forecast.DayForecast dayForecastorecast) {
    }
    @Override
    public void showForecast(Forecast forecast) {
    }
    @Override
    public void onOtherDayButtonClicked() {
    }
    @Override
    public void onOtherPlaceButtonClicked() {
    }
    @Override
    public void setOnOtherBtnCallback(IOtherPlaceButonCallback cb) {
    }
    @Override
    public void setIsOtherPlaceButtonActive(boolean isActive) {
    }
    @Override
    public void setHoldingActivity(Activity activity) {
    }
    /** Method is ment to be working in Activity-Fragment counpling, useless in here
     * @param position
     */
    @Override
    public void onDaySelected(int position) {
    }

    @Override
    public void disableNextSelectionCallbackFiring(int n, LocationData selectAfterModification) {
    }

    private IViewingController mViewingController;
    private ICityPickedFeedback mPickingFeedback;
    private ISysShapesDisplay mMapIFace;
    private LocationData mPickedCity;
    private Activity mActivity;
    private IFreePlaceMonitor mFreePlaceMonitor;

    private Map<LocationData, ShapeData> mConstantWeatherAreas;
    private LocationData mCenterOfLastSelectedArea;
    /** repetitive marker selection support */
    private LocationData mLastChoisenSavedPlace;
    private LocationData mSelectedFreePlace;
    IOnEditCallback mEditCallback;
}
