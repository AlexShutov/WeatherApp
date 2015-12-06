package com.alex.weatherapp.UIv2;

import android.app.Activity;
import android.location.Geocoder;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.MVP.IPresenter;
import com.alex.weatherapp.MVP.IView;
import com.alex.weatherapp.MVP.IViewContract;
import com.alex.weatherapp.MapsFramework.Interfacing.Shapes.ISysShapesDisplay;
import com.alex.weatherapp.UI.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIv2.CityPicker.ICityPickedFeedback;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.UIv2.FreePlaceGeoMonitor.GeoFreePlaceMonitor;
import com.alex.weatherapp.UIv2.FreePlaceGeoMonitor.IFreePlaceMonitor;
import com.alex.weatherapp.Utils.Logger;

import java.util.List;

/**
 * Created by Alex on 24.11.2015.
 */
public class UIHub implements IView {
    interface IOnLoadDoneCallback{
        void onLoadDone();
    }

    /**
     * Defines it own way for initializing viewing controller and add different
     * controllers in composite controller, if you must. Initializer instance is passed
     * during UIHub creation, but controller may be changed when needed.
     */
    public interface IViewingControllerInitializer {
        /**
        * When this interface is user, UIHub is already having ui items set and picked (spinner and
        * forecast viewer), so there are two options- whether leave it be, or add some other stuff.
                * Do nothing in a first place, default controller will be created based on passed picker and
        * forecast viewer, but if you want to add something, perhaps, alter map behaviour, use
        * mainUIController instance by adding it to result composite controller.
         * */
        IViewingController createViewingController(IViewingController mainUIController);
    }
    private class DefaultControllerCreator implements IViewingControllerInitializer{
        @Override
        public IViewingController createViewingController(IViewingController mainUIController) {
            IViewingController uiController = new ViewingController();
            uiController.assignForecastViewer(mForecastViewer);
            uiController.assignPlacePicker(mCityPicker);
            return uiController;
        }
    }
    public static class MapEnchancedControllerCreator implements IViewingControllerInitializer{
        public MapEnchancedControllerCreator(){ }
        @Override
        public IViewingController createViewingController(IViewingController mainUIController) {
            IViewingController uiController = mainUIController;
            NotifyingComposite viewingComposite = new NotifyingComposite();
            viewingComposite.addController(uiController);
            viewingComposite.setMasterController(uiController);
            if (null == mapIFace || null == activity){
                Logger.e("Can't create map viewer because wherther ISysShapesDisplay or" +
                        " Activity reference is null, switching to default controller");
                return uiController;
            }
            mMapViewer = new MapViewer();
            mMapViewer.setMapInterface(mapIFace);
            mMapViewer.setActivity(activity);
            if (null != this.placeRepetitiveClickCallback){
                mMapViewer.setEditCallback(placeRepetitiveClickCallback);
            }
            ViewingController mapController = new ViewingController();
            mapController.assignPlacePicker(mMapViewer);
            mapController.assignForecastViewer(mMapViewer);
            mMapViewer.setViewingController(mapController);
            viewingComposite.addController(mapController);
            DummyViewingController dummy = new DummyViewingController("Dummy controller");
            viewingComposite.addController(dummy);
            return viewingComposite;
        }
        public void setMapIFace(ISysShapesDisplay mapIFace){
            this.mapIFace = mapIFace;
        }
        public void setActivity(Activity activity){
            this.activity = activity;
        }
        public void setPlaceRepetitiveClickCallback(MapViewer.IOnEditCallback cb){
            placeRepetitiveClickCallback = cb;
        }
        MapViewer mMapViewer;
        MapViewer.IOnEditCallback placeRepetitiveClickCallback;
        ISysShapesDisplay mapIFace;
        Activity activity;
    }

    public UIHub(){
        mDefaultUIController = null;
        mUIController = null;
        mOnLoadDoneCallback = null;
        mFreePlaceMonitor = null;
        mMapViewer = null;
    }

    boolean mRefreshOnInit;
    public void setRefreshOnInit(boolean refresh){ mRefreshOnInit = refresh;}
    public void init(Activity activity,
                     ICityPicker cityPicker,
                     IForecastViewer forecastViewer,
                     boolean refreshOnInit,
                     IFreePlaceMonitor freePlaceMonitor){
        mActivity = activity;
        mIsPresenterConnected = false;
        initIViewInheritage();

        mForecastViewer = forecastViewer;
        mCityPicker = cityPicker;
        mCityPicker.setActivity(mActivity);
        mCityPicker.setFeedback(mCityFeedback);
        mRefreshOnInit = refreshOnInit;

        /** create default ui controller */
        IViewingControllerInitializer vci = new DefaultControllerCreator();
        mDefaultUIController = vci.createViewingController(null);
        mUIController = mDefaultUIController;


        mFreePlaceMonitor = null;
    }

    public void initEnchancedController(IViewingControllerInitializer initializer){
        if (null == initializer) return;
        mUIController = initializer.createViewingController(mDefaultUIController);
        /** a bit tricky */
        if (initializer instanceof MapEnchancedControllerCreator){
            this.mMapViewer = ((MapEnchancedControllerCreator)initializer).mMapViewer;
        }
    }
    public void switchToDefaultUIController(){
        mUIController.setOnCityPickedFeedback(null);
        mUIController = mDefaultUIController;
    }

    public IViewingController getUIController(){
        return mUIController;
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
    /** MVP- inherited and related stuff*/
    private void initIViewInheritage(){
        mPresenter = null;
        mIsPresenterConnected = false;
        mIsUIReady = true;
    }

    public void showPopup(String msg) {
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Mimics Activity's lifecycle methods
     */
    public void pause() {
        mUIController.saveState();
        mPresenter.disconnectView(this);
        //mMapFacade.suspend();
        mCityPicker.saveState();
    }
    public void resume(IPresenter presenter){
        mPresenter = presenter;
        mPresenter.setView(this);
        if (mPresenter.isPresenterReady()){
            // add some stuff with mPresenter involved
            showPopup("Presenter is connected in onResume");
            if (mRefreshOnInit) refreshContent();
        } else {
            mPresenter.setPresenterReadyCallback(new IPresenter.IPresenterReady() {
                @Override
                public void onPresenterReady(IPresenter presenter) {
                    // add some stuff in here
                    mPresenter = presenter;
                    mIsPresenterConnected = true;
                    showPopup("presenter is first created");
                    if (mRefreshOnInit) refreshContent();
                }
            });
        }
        //mMapFacade.resume()

    }

    public void refreshContent(){
        mPresenter.getListOfSavedPlaces();
        mPresenter.acquireForecastsForAllPlaces();
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
            mUIController.handleListOfPlaces(locations);
           // mDefaultUIController.handleListOfPlaces(locations);
        }
        @Override
        public void showPlacesForecasts(List<PlaceForecast> forecasts) {
        }
        @Override
        public void showPlaceForecast(PlaceForecast forecast) {
            showMsg("Forecast received for: " + forecast.getPlace().getmPlaceName());
            mUIController.handleIncomingForecast(forecast);
            //mCityPicker.restoreState();
        }
        @Override
        public void showStandalonePlaceForecast(PlaceForecast forecast) {

        }
        @Override
        public void onNewPlaceIsAddedToPlaceRegistry(LocationData placeInfo) {
            showPopup("Place is added: " + placeInfo.getmPlaceName());
            refreshContent();
        }

        /**
         * Even thought db has no saved places, ui still continues to show them
         */
        @Override
        public void onAllPlacesRemoved() {
            mUIController.clear();
        }
        @Override
        public void showOnlineForecast(PlaceForecast forecast) {

        }
        @Override
        public void showGoogleGeolookup(LocationData placeLoc, String placeName) {

        }
    };



    ICityPickedFeedback mCityFeedback = new ICityPickedFeedback() {
        @Override
        public void onCityPicked(LocationData pickedCity) {
            if (null != mOnLoadDoneCallback)  mOnLoadDoneCallback.onLoadDone();
        }
    };

    public void setOnLoadDoneCallback(IOnLoadDoneCallback cb){
        mOnLoadDoneCallback = cb;
    }

    public void createGeoMonitor(boolean activate){
        /** deactivate monitor */
        if (!activate){
            mFreePlaceMonitor = null;
            if (null != mMapViewer) mMapViewer.setFreePlaceMonitor(null);
            return;
        }
        GeoFreePlaceMonitor monitor = new GeoFreePlaceMonitor(mActivity);
        if (mMapViewer == null) return;
        mMapViewer.setFreePlaceMonitor(monitor);

        mFreePlaceMonitor = monitor;
    }


    private Activity mActivity;
    private ICityPicker mCityPicker;
    private IForecastViewer mForecastViewer;
    private IViewingController mDefaultUIController;
    private IViewingController mUIController;
    private IPresenter mPresenter;
    private boolean mIsPresenterConnected;
    private boolean mIsUIReady;

    /** this reference is instantiated from inner class (initializer) when map mode is being
     * activated and cleared on turning off map mode
     */
    MapViewer mMapViewer;
    private IFreePlaceMonitor mFreePlaceMonitor;

    private IOnLoadDoneCallback mOnLoadDoneCallback;

}
