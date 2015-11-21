package com.alex.weatherapp.UIv2;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.MVP.IPresenter;
import com.alex.weatherapp.MVP.IView;
import com.alex.weatherapp.MVP.IViewContract;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.IFeedbackShapes;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ISysShapesDisplay;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ShapesAndMarkersBehaviour;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ShapesDisplayAdapter;
import com.alex.weatherapp.MapsFramework.Interfacing.ShapesTestBehaviour;
import com.alex.weatherapp.MapsFramework.MapFacade;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.R;
import com.alex.weatherapp.UIv2.CityPicker.CityPicker;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickerChannel;
import com.alex.weatherapp.Utils.Logger;
import com.alex.weatherapp.WeatherApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Alex on 03.11.2015.
 */
public class TestActivity extends FragmentActivity implements IView,
        IFeedbackShapes, ICityPickerChannel, ICityPickedFeedback
{
    private class ViewContract implements IViewContract {
        @Override
        public void handleListOfSavedPlaces(List<LocationData> locations) {
            StringBuilder sb = new StringBuilder();
            for (LocationData ld : locations){
                sb.append(ld.getmPlaceName());
                sb.append(" ");
            }
            Logger.i("List of saved places: " + sb.toString());
        }

        @Override
        public void showPlacesForecasts(List<PlaceForecast> forecasts) {

        }

        @Override
        public void showPlaceForecast(PlaceForecast forecast) {

            Logger.i("Forecast for: " + forecast.getPlace().getmPlaceName());
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
    }
    IViewContract mViewContract;

    /** MVP- inherited and related stuff*/
    private void initIViewInheritage(){
        mPresenter = null;
        mIsPresenterConnected = false;
        mIsUIReady = true;
    }

    @Override
    public void connectToPresenter(IPresenter presenter) {
        mPresenter = presenter;
        if (presenter != null) {
            mIsPresenterConnected = true;
        } else {
            mIsPresenterConnected = false;
            Log.d("MVP error", "Failed to connect to Presenter");
        }
    }

    @Override
    public boolean isPresenterConnected() { return mPresenter != null && mIsPresenterConnected; }

    @Override
    public boolean isUIReady() { return mIsUIReady; }

    @Override
    public IViewContract getContract() { return mViewContract;  }

    private IPresenter mPresenter;
    boolean mIsPresenterConnected;
    boolean mIsUIReady;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_layout);


        final Button testBtn = (Button)findViewById(R.id.idc_ta_btn_test);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
        mViewContract = new ViewContract();
        initIViewInheritage();

        final Button test1 = (Button) findViewById(R.id.idc_ta_btn_test1);
        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test1();
            }
        });
        final Button test2 = (Button) findViewById(R.id.idc_ta_btn_test2);
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test2();
            }
        });


        validateGPlayReady();

        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.idc_ta_map);
        mMap = mMapFragment.getMap();
        mMapFacade = new MapFacade(mMap);

        CameraUpdate newCamera = CameraUpdateFactory
                .newLatLngZoom(new LatLng(37.4218, -122.0840), 14);
        mMap.moveCamera(newCamera);

        Deployer deployer = new Deployer();
        deployer.setFacade(mMapFacade);

        ShapesAndMarkersBehaviour behaviour = new ShapesAndMarkersBehaviour();
        behaviour.activate(mMapFacade);
        behaviour.setFeedbackInterface(this);
        mIface = behaviour.getUserInterface();

        /////////////////////////////////////////////////////////////////////////////////
        mCityPicker = new CityPicker(R.id.idc_ta_city_picker);
        mCityPicker.setActivity(this);
        mCityPicker.setFeedback(this);

        ArrayList<LocationData> cities = new ArrayList<>();
        LocationData l =  new LocationData(0.0f, 0.0f, "Place 1");
        cities.add(l);
        l = new LocationData(1.0f, 0.0f, "The hell");
        cities.add(l);
        mCityPicker.setCities(cities);
        mCityPicker.refresh();
    }

    static int placeCnt= 0;
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


    @Override
    public void cityPicked(LocationData cityPicked) {
        mCityPicker.cityPicked(cityPicked);
    }

    @Override
    public void onCityPicked(LocationData pickedCity) {
        if (null == pickedCity){
            showPopup("Nothing is selected");
        } else {
            showPopup("Place selected: " + pickedCity.getmPlaceName());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter == null){
            WeatherApplication app = (WeatherApplication)getApplication();
            mPresenter = app.getDefaultPresenter();
        }
        mPresenter.setView(this);
        if (mPresenter.isPresenterReady()){
            // add some stuff with mPresenter involved
            showPopup("Presenter is connected in onResume");
        } else {
            mPresenter.setPresenterReadyCallback(new IPresenter.IPresenterReady() {
                @Override
                public void onPresenterReady(IPresenter presenter) {
                    // add some stuff in here
                    showPopup("presenter is first created");
                }
            });
        }
        mMap.setMyLocationEnabled(true);
        mMapFacade.resume();

        mCityPicker.restoreState();
    }

    @Override
    protected void onPause() {
        mPresenter.disconnectView(this);
        mMap.setMyLocationEnabled(false);
        mMapFacade.suspend();
        mCityPicker.saveState();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    void showMsg(String msg){
        TextView tv = (TextView)findViewById(R.id.idc_ta_text_msg);
        tv.setText(msg);
    }
    public void showPopup(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void test(){
        showMsg("Test");
        CircularRegionData circle = new CircularRegionData(new LatLng(37.4318, -122.0840), 400);
        circle.setShapeName("Added_circle");
        mIface.addCircularArea(circle);
        float lon = -122.1040f;

        RectRegionData rect = new RectRegionData();
        rect.setShapeName("rect1");
        rect.setRightBottom(new LatLng(37.4169, -122.0890));
        rect.setTopLeft(new LatLng(37.4269, -122.0790));
        mIface.addRectangularArea(rect);

        rect = new RectRegionData();
        rect.setShapeName("rect2");
        rect.setTopLeft(new LatLng(37.4269, -122.0890));
        rect.setRightBottom(new LatLng(37.4169, -122.10));
        mIface.addRectangularArea(rect);


        circle = new CircularRegionData(new LatLng(37.4318, -122.0940), 400);
        circle.setShapeName("Circle");
        mIface.addCircularArea(circle);


        LocationData tmpLoc = new LocationData(37.4218, -122.0840, "Some place");
        PlaceData tmpPlace = new PlaceData(tmpLoc);
        //dataFamily.addEntity(tmpPlace);
        tmpPlace.setMarkerType(PlaceData.MarkerType.BitmapIcon);
        tmpPlace.setIsDraggable(true);
        List<PlaceData> info = new ArrayList<>();
        info.add(tmpPlace);
        mIface.addInfoMarker(tmpPlace);
        PlaceData t2 = new PlaceData(tmpPlace);
        //mIface.removeInfoMarekr(t2);
        mIface.selectShape("Circle", true);
        mIface.deselectShape("Circle");



    }

    /** Methods related to maps and gplay library */
    void validateGPlayReady(){
        /** Check if Google Play Services is up to date */
        switch (GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this)){
            case ConnectionResult.SUCCESS:
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(this,
                        "Maps service requires an update, "
                                + "please open Google Play.",
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            default: Toast.makeText(this,
                    "Maps are not availible on this device.", Toast.LENGTH_SHORT).show();
                finish();
                return;
        }
    }

    void performMapAction(){
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        LatLng mapCenter;
        if (location != null){
            mapCenter = new LatLng(location.getLatitude(), location.getLongitude());
        }else {
            mapCenter = new LatLng(37.4218, -122.0840);
        }
        CameraUpdate newCamera =
                CameraUpdateFactory.newLatLngZoom(mapCenter, 13);
        mMap.moveCamera(newCamera);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
/////////////////////////////////////////////////////////////////////
    /**
    // add some dummy marker;
    LocationData tmpLoc = new LocationData(37.4218, -122.0840, "Some place");
    PlaceData tmpPlace = new PlaceData(tmpLoc);
    //dataFamily.addEntity(tmpPlace);
    tmpPlace.setMarkerType(PlaceData.MarkerType.BitmapIcon);
    tmpPlace.setIsDraggable(false);
*/
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
/////////////////////////////////////////////////////////////////////
    private MapFacade mMapFacade;
    ISysShapesDisplay mIface;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    private CityPicker mCityPicker;
}
