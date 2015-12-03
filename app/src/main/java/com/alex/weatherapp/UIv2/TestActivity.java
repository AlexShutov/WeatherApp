package com.alex.weatherapp.UIv2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MVP.IPresenter;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.IFeedbackShapes;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ISysShapesDisplay;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ShapesAndMarkersBehaviour;
import com.alex.weatherapp.MapsFramework.MapFacade;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;
import com.alex.weatherapp.R;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.CityPicker;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickerChannel;
import com.alex.weatherapp.UIv2.ForecastViewer.SimpleForecastViewer;
import com.alex.weatherapp.Utils.Logger;
import com.alex.weatherapp.WeatherApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Alex on 03.11.2015.
 */
public class TestActivity extends FragmentActivity implements
         ICityPickerChannel, ICityPickedFeedback,
        MyMapFragment.IOnMapReady, MapViewer.IOnEditCallback
{
    private static String IS_MAP_ENABLED = "is_map_enabled";
    private void saveMapEnabled(boolean isEnabled){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        prefs.edit().putBoolean(IS_MAP_ENABLED, isEnabled).apply();
    }
    private boolean isMapActive(){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getBoolean(IS_MAP_ENABLED, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Logger.w("TRACE: onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_layout);
        mPresenter = null;
        mIsMapSupportEnabled = false;
        mSelectedFreePlace = null;

        // mMap = mMapFragment.getMap();
       // initMapFacade(mMap);

        final Button enableMapBtn = (Button) findViewById(R.id.idc_ta_enable_map);
        enableMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableMapsSupport(R.id.idc_ta_second_map, null, false);
            }
        });
        final Button disableMapBtn = (Button)findViewById(R.id.idc_ta_disable_map);
        disableMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableMapSupport(true);
            }
        });

        /////////////////////////////////////////////////////////////////////////////////
        mCityPicker = new CityPicker(R.id.idc_ta_city_picker);
        mCityPicker.setActivity(this);
        mCityPicker.setFeedback(this);

        int[] frames = new int[]{R.id.idc_ta_short_forecast_frame,
                R.id.idc_ta_short_forecast_frame_2,
                R.id.idc_ta_short_forecast_frame_3};
        mForecastViewer = new SimpleForecastViewer(frames, this);

        mHub = new UIHub();
        final boolean showMap = isMapActive();
        if (showMap){
            /** Order of AppHub initialization amd enabling map support is importaint.
             * Map must be enabled only when hub is initialized, because it alters
             * ViewingController inside AppHub
             */
            mHub.init(this, mCityPicker, mForecastViewer, false, null);
            enableMapsSupport(R.id.idc_ta_second_map, null, true);
        }else {
            mHub.init(this, mCityPicker, mForecastViewer, true, null);
        }

        //enableMapsSupport(R.id.idc_ta_second_map);
    }

    private boolean mIsLifecycleMapEnabling;
    private void enableMapsSupport(int mapResourceID,
                                   GoogleMapOptions options,
                                   boolean isLifeCycleCall){
        Logger.w("TRACE: enableMapSupport()");
        if (!validateGPlayReady()){
            showPopup("Maps are not available on this device");
            return;
        }
        mIsLifecycleMapEnabling = isLifeCycleCall;
        mCityPicker.saveState();
        FragmentManager fm = getFragmentManager();
        MyMapFragment secondMap = MyMapFragment.newInstance(options);
        fm.beginTransaction().replace(mapResourceID , secondMap, MyMapFragment.TAG_MY_MAP_FRAGMENT)
                .commit();
        mIsMapSupportEnabled = true;
        saveMapEnabled(true);

    }
    private void disableMapSupport(boolean detachFragment){
        if (!isMapSupportEnabled()){
            Logger.w("Map support hasn't been enabled");
            return;
        }
        Logger.w("TRACE: disableMapSupport()");
        if (null != mMapFacade) {
            mMapFacade.suspend();
        }
        mMapFacade = null;
        mMapIface = null;
        mMap = null;
        mIsMapSupportEnabled = false;
        if (detachFragment) saveMapEnabled(false);
        FragmentManager fm = getFragmentManager();
        Fragment mapFragment = fm.findFragmentByTag(MyMapFragment.TAG_MY_MAP_FRAGMENT);
        if (null != mapFragment){
            GoogleMap map = ((MyMapFragment) mapFragment).getMap();
            map.clear();
            if (detachFragment){
                fm.beginTransaction().remove(mapFragment).commit();
            }
        }
        /** When screen has no map, we can't add a new place -> don't need a
         * GeoMonitor */
        mHub.createGeoMonitor(false);
        mHub.switchToDefaultUIController();
    }

    private boolean isMapSupportEnabled(){ return mIsMapSupportEnabled; }

    private void initMapFacade(GoogleMap map){
        Logger.w("TRACE: initMapFacade()");
        mMapFacade = new MapFacade(map);
        Deployer deployer = new Deployer();
        deployer.setFacade(mMapFacade);
        ShapesAndMarkersBehaviour behaviour = new ShapesAndMarkersBehaviour();
        behaviour.activate(mMapFacade);
        behaviour.setFeedbackInterface(mLoggingMapFeedback);
        mMapIface = behaviour.getUserInterface();
    }

     /** Now we have a ready map instance, so we can safely instantiate our
     * MapFacade and configure our UIHub for using maps. MyMapFragment creation is triggered
     * in onCreateMethod, when we tell system to place that fragment.
     * @param map
     */
     @Override
     public void acceptMapInstance(GoogleMap map) {
         Logger.w("TRACE: acceptMapInstance()");
         mMap = map;
         initMapFacade(map);

         UIHub.MapEnchancedControllerCreator mapViewerCreator = new UIHub.MapEnchancedControllerCreator();
         mapViewerCreator.setActivity(this);
         mapViewerCreator.setMapIFace(mMapIface);
         mapViewerCreator.setPlaceRepetitiveClickCallback(this);
         mHub.initEnchancedController(mapViewerCreator);
         mHub.setRefreshOnInit(true);
         /** geomonitor allows to request place name by inverse geolookup and
          * display it in picker when free place is selected on a map
          */
         mHub.createGeoMonitor(true);
         mHub.refreshContent();
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
            showMsg("Place selected: " + pickedCity.getmPlaceName());
        }
    }

    /** Inherited from MapViewer.IOnEditCallback
     * */
    @Override
    public void handleRepeatingSelection(final LocationData place) {

    }

    /** Activity keeps track of last free place observed (any place on map), and if
     * user is currently tapped any place on a map, this method gets fired. It saves that place
     * and enables options menu to save that place.
     * @param place
     */
    @Override
    public void handleFreePlace(LocationData place) {
        mSelectedFreePlace = place;
        showMsg("Free place is selected ");
    }

    /** This MapViewer's callback method tells Activity that user have selected saved place.
     * Activity, in turn, clears reference to free place and save options menu item becones
     * unavailible
     * @param selectedPlace
     */
    @Override
    public void handleSelectionOfSavedPlace(LocationData selectedPlace) {
        /** clear free place reference */
        mSelectedFreePlace = null;
        showMsg("Saved place is selected");
    }

    /** Presenter only capable of removing all places, so removing just one will force
     * to update forecasts for all places, because to do that we need memorize all places but the
     * selected one, then clear all places and add the rest.
     * @param place
     */
    private void removeSavedPlaceAndItsForecast(LocationData place){
        List<LocationData> knownPlaces = mHub.getUIController().getKnownPlaces();
        knownPlaces.remove(place);
        mPresenter.removeAllPlaces();
        for (LocationData p : knownPlaces){
            mPresenter.addNewPlace(p);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.w("TRACE: onStart()");
        if (mPresenter == null){
            WeatherApplication app = (WeatherApplication)getApplication();
            mPresenter = app.getDefaultPresenter();
        }
        mHub.resume(mPresenter);
        /** we may not be using maps right now */
        if (null != mMapFacade) {
            Logger.w("TRACE: mMapFacade.resume();");
            mMapFacade.resume();
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        Logger.w("TRACE: onStop()");
        mHub.pause();
        if (null != mMapFacade) {
            Logger.w("TRACE: mMapFacade.suspend()");
            mMapFacade.suspend();
            mMap.setMyLocationEnabled(false);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.w("TRACE: onDestroy()");
        if (mIsMapSupportEnabled){
            mCityPicker.saveState();
            disableMapSupport(false);
        }
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
    boolean validateGPlayReady(){
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
                return false;
            default: Toast.makeText(this,
                    "Maps are not availible on this device.", Toast.LENGTH_SHORT).show();
                finish();
                return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_add_new_place:
                addNewPlace();
                break;
            case R.id.menu_main_add_predefined_places:

                break;
            case R.id.menu_main_clear_all_places:
                mPresenter.removeAllPlaces();
                break;
            case R.id.menu_main_update_online:

                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    private static int placeCnt = 0;
    private void addNewPlace(){
        if (null == mSelectedFreePlace){
            showPopup("You must select place on map first");
            return;
        }
        mPresenter.addNewPlace(mSelectedFreePlace);
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
    private MapFacade mMapFacade;
    ISysShapesDisplay mMapIface;

    private IPresenter mPresenter;

    private boolean mIsMapSupportEnabled;
    private GoogleMap mMap;

    private ICityPicker mCityPicker;
    private IForecastViewer mForecastViewer;
    /** place, selected by user, which is not saved */
    private LocationData mSelectedFreePlace;

    private UIHub mHub;

    IFeedbackShapes mLoggingMapFeedback = new IFeedbackShapes() {
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
}
