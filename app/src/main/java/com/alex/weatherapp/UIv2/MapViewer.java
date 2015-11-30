package com.alex.weatherapp.UIv2;


import android.app.Activity;
import android.graphics.drawable.shapes.Shape;
import android.location.Location;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUndergroundGeolookupData;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.IFeedbackShapes;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ISysShapesDisplay;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.ShapeData;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
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
    public MapViewer(){
    }
    private void init(){
        mMapIFace = null;
        /** gonna be rewritten anyway */
        mActivity = null;
        mPickedCity = null;
        mSetFeedback = null;
        mViewingController = null;
        mCenterOfLastSelectedArea = null;
    }
    public void setMapInterface(ISysShapesDisplay mapInterface){
        mMapIFace = mapInterface;
        mMapIFace.setFeedbackInterface(mMapFeedback);
    }

    @Override
    public void setViewingController(IViewingController viewingController) {
        mViewingController = viewingController;
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
        final String areaNamePrefix = "Area for: ";
        if (null != mConstantWeatherAreas){
            for (LocationData ld : mConstantWeatherAreas.keySet()){
                String areaName = areaNamePrefix + ld.getmPlaceName();
                mMapIFace.removeShape(areaName);
            }
            mConstantWeatherAreas.clear();
        }else {
            mConstantWeatherAreas = new TreeMap<>();
        }
        mMapIFace.removeInfoMarkers();
        for (LocationData place : places){
            PlaceData markerData = new PlaceData(place);
            markerData.setMarkerType(PlaceData.MarkerType.Standard);
            mMapIFace.addInfoMarker(markerData);

            LatLng center = new LatLng(place.getLat(), place.getLon());
            CircularRegionData weatherArea = new CircularRegionData(center, CONSTANT_WEATHER_RADII);
            weatherArea.setShapeName(areaNamePrefix + place.getmPlaceName());
            mConstantWeatherAreas.put(place, weatherArea);
            mMapIFace.addCircularArea(weatherArea);
        }
    }
    @Override
    public void addCity(LocationData city) {
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
    }
    @Override
    public boolean isHaving(LocationData place) {
        return false;
    }
    @Override
    public void pickCity(LocationData city) {
        mPickedCity = city;
        Logger.i("MapViewer.pickCity("+ city.getmPlaceName() + ");");
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
    }
    @Override
    public void saveState(LocationData pickedLocation) {
    }
    @Override
    public void restoreState() {
    }
    @Override
    public void refresh() {
    }

    @Override
    public void setFeedback(ICityPickedFeedback feedbackImpl) {
        mSetFeedback = feedbackImpl;
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
        pickCity(cityPicked);
    }

    private IFeedbackShapes mMapFeedback = new IFeedbackShapes() {
        @Override
        public void onCircularRegionSelected(CircularRegionData data) {
            List<LocationData> places = mViewingController.getKnownPlaces();
            LatLng t = data.getCenter();
            LocationData center = new LocationData(t.latitude, t.longitude);
            LocationData areaAround = null;
            for (LocationData p : places) {
                center.setmPlaceName(p.getmPlaceName());
                if (center.equals(p)) {
                    areaAround = p;
                    break;
                }
            }
            if (null != areaAround) {
                if (null != mSetFeedback) {
                    mSetFeedback.onCityPicked(areaAround);
                }
            }
            mCenterOfLastSelectedArea = areaAround;
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
            boolean isNew = false;
            if (null == mCenterOfLastSelectedArea){
                isNew = true;
            }else {
                float[] dist = new float[1];
                LocationData c = mCenterOfLastSelectedArea;
                Location.distanceBetween(c.getLat(), c.getLon(), place.getLat(), place.getLon(), dist);
                isNew = (dist[0] >= CONSTANT_WEATHER_RADII);
            }
            if (isNew) {
                String msg = "new place is pinned: (" + place.getLat() + ", " + place.getLon() + ")";
                Logger.i(msg);
                if (null != mActivity) {
                    Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onInfoMarkerClick(PlaceData infoMarker) {
            if (null != mSetFeedback){
                mSetFeedback.onCityPicked(infoMarker.getLocation());
            }
        }

        @Override
        public void showServiceMessage(String msg) {
            Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
        }
    };

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


    private IViewingController mViewingController;
    private ICityPickedFeedback mSetFeedback;
    private ISysShapesDisplay mMapIFace;
    private LocationData mPickedCity;
    private Activity mActivity;

    private Map<LocationData, ShapeData> mConstantWeatherAreas;
    private LocationData mCenterOfLastSelectedArea;
}
