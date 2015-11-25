package com.alex.weatherapp.UIv2;

import android.app.Activity;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.MVP.IPresenter;
import com.alex.weatherapp.MVP.IView;
import com.alex.weatherapp.MVP.IViewContract;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.IFeedbackShapes;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ISysShapesDisplay;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ShapesAndMarkersBehaviour;
import com.alex.weatherapp.MapsFramework.MapFacade;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 24.11.2015.
 */
public class AppHub implements IView {
    public AppHub(){
    }

    public void init(Activity activity,
                     GoogleMap map,
                     ICityPicker cityPicker,
                     IForecastViewer forecastViewer){
        mActivity = activity;
        mIsRefreshRequired = true;
        initIViewInheritage();
        initMapFacade(map);

        mForecastViewer = forecastViewer;
        mCityPicker = cityPicker;
        mCityPicker.setActivity(mActivity);
        mCityPicker.setFeedback(mCityFeedback);

        mViewingController = new ViewingController();
        mViewingController.assignForecastViewer(mForecastViewer);
        mViewingController.assignPlacePicker(mCityPicker);

        fillTestCities();
        showTestForecast();
        //test();
    }

    /** Some stuff for testing */
    static int placeCnt= 0;
    private void fillTestCities(){
        ArrayList<LocationData> cities = new ArrayList<>();
        LocationData l =  new LocationData(0.0f, 0.0f, "Place 1");
        cities.add(l);
        l = new LocationData(666.0f, 666.0f, "The hell");
        cities.add(l);

        placeCnt = 2;
        for (int i = 0; i < 10; ++i){
            String name = "Place " + String.valueOf(placeCnt);
            LocationData placeN = new LocationData( 47.0f + placeCnt,47.0f + placeCnt, name);
            cities.add(placeN);
            placeCnt++;
        }
        mViewingController.handleListOfPlaces(cities);
        //mCityPicker.setCities(cities);
        //mCityPicker.refresh();
    }
    public void test(){
        showMsg("Test");
        CircularRegionData circle = new CircularRegionData(new LatLng(37.4318, -122.0840), 400);
        circle.setShapeName("Added_circle");
        mMapIface.addCircularArea(circle);
        float lon = -122.1040f;

        RectRegionData rect = new RectRegionData();
        rect.setShapeName("rect1");
        rect.setRightBottom(new LatLng(37.4169, -122.0890));
        rect.setTopLeft(new LatLng(37.4269, -122.0790));
        mMapIface.addRectangularArea(rect);

        rect = new RectRegionData();
        rect.setShapeName("rect2");
        rect.setTopLeft(new LatLng(37.4269, -122.0890));
        rect.setRightBottom(new LatLng(37.4169, -122.10));
        mMapIface.addRectangularArea(rect);

        circle = new CircularRegionData(new LatLng(37.4318, -122.0940), 400);
        circle.setShapeName("Circle");
        mMapIface.addCircularArea(circle);

        LocationData tmpLoc = new LocationData(37.4218, -122.0840, "Some place");
        PlaceData tmpPlace = new PlaceData(tmpLoc);
        //dataFamily.addEntity(tmpPlace);
        tmpPlace.setMarkerType(PlaceData.MarkerType.BitmapIcon);
        tmpPlace.setIsDraggable(true);
        List<PlaceData> info = new ArrayList<>();
        info.add(tmpPlace);
        mMapIface.addInfoMarker(tmpPlace);
        PlaceData t2 = new PlaceData(tmpPlace);
        //mMapIface.removeInfoMarekr(t2);
        mMapIface.selectShape("Circle", true);
        mMapIface.deselectShape("Circle");
    }
    public void test1(){
        String name = "Place " + String.valueOf(placeCnt);
        LocationData placeN = new LocationData( 47.0f + placeCnt,47.0f + placeCnt, name);
        mCityPicker.addCity(placeN);
        placeCnt++;
        mPresenter.addNewPlace(placeN);
        mPresenter.getListOfSavedPlaces();
    }
    public void test2(){
        String name = "Place " + String.valueOf(placeCnt-1);
        LocationData placeN = new LocationData( 47.0f + placeCnt-1,47.0f + placeCnt-1, name);
        //mCityPicker.removeCity(placeN);
        showMsg(mCityPicker.getPickedCity().getmPlaceName());
        mCityPicker.pickCity(placeN);

        //mCityPicker.removeCity("The hell");
    }
    private Forecast.DayForecast copy(Forecast.DayForecast df){
        Forecast.DayForecast tmp = new Forecast.DayForecast();
        tmp.conditions = df.conditions;
        tmp.tempHigh = df.tempHigh;
        tmp.tempLow = df.tempLow;
        tmp.dayOfYear = df.dayOfYear;
        tmp.year = df.year;
        return tmp;
    }
    public void showTestForecast(){
        Forecast.DayForecast df = new Forecast.DayForecast();
        df.conditions = "Day 1. Rise and shine";
        df.tempLow= 0;
        df.tempHigh = 10;
        df.dayOfYear = 200;
        df.year = 2015;
        Forecast f = new Forecast();
        f.mDayForecasts.add(df);
        Forecast.DayForecast tmp = copy(df);
        tmp.conditions = "Day 2.";
        f.mDayForecasts.add(tmp);
        tmp = copy(df);
        tmp.conditions = "Day 3.";
        f.mDayForecasts.add(tmp);
        mForecastViewer.showForecast(f);
    }

    /** MVP- inherited and related stuff*/
    private void initIViewInheritage(){
        mPresenter = null;
        mIsPresenterConnected = false;
        mIsUIReady = true;
    }
    private void initMapFacade(GoogleMap map){
        mMapFacade = new MapFacade(map);
        Deployer deployer = new Deployer();
        deployer.setFacade(mMapFacade);
        ShapesAndMarkersBehaviour behaviour = new ShapesAndMarkersBehaviour();
        behaviour.activate(mMapFacade);
        behaviour.setFeedbackInterface(mMapFeedback);
        mMapIface = behaviour.getUserInterface();

    }

    public void showPopup(String msg) {
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Mimics Activity's lifecycle methods
     */
    public void pause() {
        mPresenter.disconnectView(this);
        mMapFacade.suspend();
        mCityPicker.saveState();
        mIsRefreshRequired = true;
    }
    public void resume(IPresenter presenter){
        mPresenter = presenter;
        mPresenter.setView(this);
        if (mPresenter.isPresenterReady()){
            // add some stuff with mPresenter involved
            showPopup("Presenter is connected in onResume");
        } else {
            mPresenter.setPresenterReadyCallback(new IPresenter.IPresenterReady() {
                @Override
                public void onPresenterReady(IPresenter presenter) {
                    // add some stuff in here
                    mPresenter = presenter;
                    mIsPresenterConnected = true;
                    showPopup("presenter is first created");
                    if (mIsRefreshRequired) {
                        refreshContent();
                        mIsRefreshRequired = false;
                    }
                }
            });
        }
        mMapFacade.resume();
        if (mIsRefreshRequired){
            refreshContent();
            mIsRefreshRequired = false;
        }
    }

    public void refreshContent(){
        mPresenter.getListOfSavedPlaces();
        mCityPicker.restoreState();
    }

    /** Inherited from IView */
    @Override
    public void connectToPresenter(IPresenter presenter) {
        mPresenter = presenter;
        if (presenter != null) {
            mIsPresenterConnected = true;
        } else {
            mIsPresenterConnected = false;
            Logger.e("MVP error", "Failed to connect to Presenter");
        }
    }

    @Override
    public boolean isPresenterConnected() {
        return mPresenter != null && mIsPresenterConnected;
    }

    @Override
    public boolean isUIReady() { return mIsUIReady; }

    @Override
    public void finish() {
    }

    @Override
    public IViewContract getContract() {
        return mViewContract;
    }
    private void showMsg(String msg){
        ((TestActivity)mActivity).showMsg(msg);
    }


    private IViewContract mViewContract = new IViewContract() {
        @Override
        public void handleListOfSavedPlaces(List<LocationData> locations) {
            showPopup("acquired " + locations.size() + " places");
        }

        @Override
        public void showPlacesForecasts(List<PlaceForecast> forecasts) {

        }

        @Override
        public void showPlaceForecast(PlaceForecast forecast) {

        }

        @Override
        public void showStandalonePlaceForecast(PlaceForecast forecast) {

        }

        @Override
        public void onNewPlaceIsAddedToPlaceRegistry(LocationData placeInfo) {

        }

        @Override
        public void onAllPlacesRemoved() {

        }

        @Override
        public void showOnlineForecast(PlaceForecast forecast) {

        }

        @Override
        public void showGoogleGeolookup(LocationData placeLoc, String placeName) {

        }
    };

    IFeedbackShapes mMapFeedback = new IFeedbackShapes() {
        @Override
        public void showServiceMessage(String msg) {
            showPopup(msg);
        }
        @Override
        public void onCircularRegionSelected(CircularRegionData data) {
            showMsg("Circle is selected: " + data.getShapeName());
        }
        @Override
        public void onRectRegionSelected(RectRegionData data) {
            showMsg("Rectagle selected: " + data.getShapeName());
        }
        @Override
        public void onNothingSelected() {
            showMsg("nothing is selected");
        }
        @Override
        public void onNewPlacePinned(LocationData place) {
            showPopup("new chosen locaton: (" + place.getLat() + ", " + place.getLon() + ")");
        }
        @Override
        public void onInfoMarkerClick(PlaceData infoMarker) {
            showMsg("Info marker is clicked: " + infoMarker.getLocation().getmPlaceName());
        }
    };

    ICityPickedFeedback mCityFeedback = new ICityPickedFeedback() {
        @Override
        public void onCityPicked(LocationData pickedCity) {

        }
    };

    private Activity mActivity;
    private MapFacade mMapFacade;
    private ICityPicker mCityPicker;
    private IForecastViewer mForecastViewer;

    private IViewingController mViewingController;

    private ISysShapesDisplay mMapIface;

    private IPresenter mPresenter;
    private boolean mIsRefreshRequired;
    private boolean mIsPresenterConnected;
    private boolean mIsUIReady;

}
