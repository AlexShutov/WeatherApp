package com.alex.weatherapp.UIv2;

import android.app.Activity;
import android.app.FragmentManager;
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
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ShapesDisplayAdapter;
import com.alex.weatherapp.MapsFramework.Interfacing.ShapesTestBehaviour;
import com.alex.weatherapp.MapsFramework.MapFacade;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.R;
import com.alex.weatherapp.UI.PlaceForecastViewer.ForecastViewer;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UI.PlacesViewer.RegistryOfPlaces;
import com.alex.weatherapp.UIv2.CityPicker.CityPicker;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickerChannel;
import com.alex.weatherapp.UIv2.ForecastViewer.DayForecastSimpleViewer;
import com.alex.weatherapp.UIv2.ForecastViewer.SimpleForecastViewer;
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
public class TestActivity extends FragmentActivity implements
         ICityPickerChannel, ICityPickedFeedback
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_layout);

        mPresenter = null;
        final Button testBtn = (Button)findViewById(R.id.idc_ta_btn_test);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHub.test();
            }
        });

        final Button test1 = (Button) findViewById(R.id.idc_ta_btn_test1);
        test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHub.test1();
            }
        });
        final Button test2 = (Button) findViewById(R.id.idc_ta_btn_test2);
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHub.test2();
            }
        });


        validateGPlayReady();

        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.idc_ta_map);
        mMap = mMapFragment.getMap();

        CameraUpdate newCamera = CameraUpdateFactory
                .newLatLngZoom(new LatLng(37.4218, -122.0840), 14);
        mMap.moveCamera(newCamera);

        /////////////////////////////////////////////////////////////////////////////////
        mCityPicker = new CityPicker(R.id.idc_ta_city_picker);
        mCityPicker.setActivity(this);
        mCityPicker.setFeedback(this);

        int[] frames = new int[]{R.id.idc_ta_short_forecast_frame,
                R.id.idc_ta_short_forecast_frame_2,
                R.id.idc_ta_short_forecast_frame_3};
        mForecastViewer = new SimpleForecastViewer(frames, this);

        mHub = new AppHub();
        mHub.init(this, mMap, mCityPicker, mForecastViewer);
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
        mHub.resume(mPresenter);
        mMap.setMyLocationEnabled(true);
   //     mMapFacade.resume();

    }

    @Override
    protected void onPause() {
        mHub.pause();
        mMap.setMyLocationEnabled(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void showMsg(String msg){
        TextView tv = (TextView)findViewById(R.id.idc_ta_text_msg);
        tv.setText(msg);
    }
    public void showPopup(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
    /*
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
    */
/////////////////////////////////////////////////////////////////////
  //  private MapFacade mMapFacade;
   // ISysShapesDisplay mMapIface;
    private IPresenter mPresenter;

    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;

    private CityPicker mCityPicker;
    private IForecastViewer mForecastViewer;

    private AppHub mHub;
}
