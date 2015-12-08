package com.alex.weatherapp.UIDetailed;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.MVP.IPresenter;
import com.alex.weatherapp.MVP.IView;
import com.alex.weatherapp.MVP.IViewContract;
import com.alex.weatherapp.R;
import com.alex.weatherapp.UIDetailed.PlacesViewer.IPlacesViewer;
import com.alex.weatherapp.UIDynamic.UIDynamicActivity;
import com.alex.weatherapp.WeatherApplication;

import java.util.List;

public class UIDetailedActivity extends Activity
implements IView, IViewContract, ILinkToHolderActivity
{

    /**
     * Here activity is the contract itself, but it may come handy to have mock contract for
     * the screen, which, perhaps, doesn't want to do everything, or just some logging decoration
     * @return
     */
    @Override
    public IViewContract getContract() {
        return this;
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
    public boolean isPresenterConnected() {
        return mPresenter != null && mIsPresenterConnected;
    }

    @Override
    public boolean isUIReady() {
        return mIsUIReady;
    }

    @Override
    public void handleListOfSavedPlaces(List<LocationData> locations) {
        /** save number of places, useful in tracking online update state */
        mNumOfPlaces = locations.size();
        mViewer.showPlaces(locations);
    }
    @Override
    public void showPlacesForecasts(List<PlaceForecast> forecasts) {
        Log.d("View", "void showPlacesForecasts(List<PlaceForecast> forecasts)");
        String msg = getResources().getString(R.string.ids_popup_all_forecasts_is_loaded) +
                forecasts.size();
        showPopup(msg);
    }
    LocationData savedPlace;
    PlaceForecast savedPlaceForecast;
    int selectedPosition;
    @Override
    public void showPlaceForecast(PlaceForecast forecast) {
        if (mUpdatingOnline) {
            mOnlineResponseCnt++;
            /** first come local the online responces */
            if (mOnlineResponseCnt == 2 * mNumOfPlaces) {
                mUpdatingOnline = false;
                mPresenter.setForceNetworkUpdate(false);
                mOnlineResponseCnt = 0;
            }
        }
        Log.d("View", "void showPlaceForecast(PlaceForecast forecast)");
        if (forecast.getForecast().mDayForecasts.size() == 0)
            return;
        mViewer.showPlaceForecast(forecast);
        if (null != savedPlace && savedPlace.equals(forecast.getPlace())) {
            savedPlaceForecast = forecast;
        }
        selectedPosition = mViewer.getPlaceViewer().getPlaceListPostion(savedPlace);
        onPlaceSelected(selectedPosition);
    }

    @Override
    public void showOnlineForecast(PlaceForecast forecast) {
        Log.d("View", "void showOnlineForecast(PlaceForecast forecast)");
    }
    @Override
    public void showStandalonePlaceForecast(PlaceForecast forecast) {
        Log.d("View", "showStandalonePlaceForecast(PlaceForecast forecast)");
        if (forecast == null || forecast.getForecast().mDayForecasts.size() == 0) {
            showPopup("Arrived forecast is empty");
            return;
        }
        showPopup("Standalone place forecast has arrived: " +
                forecast.getForecast().mDayForecasts.get(0).dayTextForecast);
    }

    @Override
    public void showGoogleGeolookup(LocationData placeLoc, String placeName) {
        Log.d("View", "void showGoogleGeolookup(LocationData placeLoc, String placeName)");
    }

    @Override
    public void onNewPlaceIsAddedToPlaceRegistry(LocationData placeInfo) {
        Log.d("View", "new place is added " + placeInfo.getLat() + " " + placeInfo.getLon());
    }

    /** After all places is removed, aquire them again, so IView's method 'showAllPlaces'
     * will force IViewer to display screen for emty place list
     */
    @Override
    public void onAllPlacesRemoved() {
        showPopup(getResources().getString(R.string.ids_popup_add_places_deleted));
        mViewer.showPlaceForecast(new PlaceForecast(null, null));
        mPresenter.getListOfSavedPlaces();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPresenter == null){
            WeatherApplication app = (WeatherApplication)getApplication();
            mPresenter = app.getDefaultPresenter();
        }
        mPresenter.setView(this);
        /** Interesting moment - model and presenter are kept persistently in
         * Application object, so on activity's first start lookup and ui update will
         * be triggered by callback from presenter when service is bound.
         * On next times (after screen rotation) presenter is already ready, so we don't need
         * callback and can just start UI update directly. If Activity gets destroyed the
         * first time, callback gonna be disabled in onPause(..) (in .disconnectView())
         */
        if (mPresenter.isPresenterReady()){
            lookupOnMVPReady(mPresenter);
        } else {
            mPresenter.setPresenterReadyCallback(new IPresenter.IPresenterReady() {
                @Override
                public void onPresenterReady(IPresenter presenter) {
                    lookupOnMVPReady(presenter);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        mPresenter.setForceNetworkUpdate(false);
        mPresenter.disconnectView(this);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumOfPlaces = 0;
        mUpdatingOnline = false;
        mOnlineResponseCnt = 0;
        initIViewInheritage();
        /** Build layout holder based on device orientation */
        View v = findViewById(R.id.idc_main_right_frame);
        boolean isSinglePane = (v == null);

        IHolderBuilder builder = new ViewerAndDataHolder.Builder();
        builder.setLinkToHoldingActivity(this);

        if (isSinglePane){
            builder.setDisplayingMode(DisplayingMode.MODE_SINGLE_FRAME);
            builder.setFrameID(R.id.idc_main_left_frame);
        } else {
            builder.setDisplayingMode(DisplayingMode.MODE_TWO_FRAMES);
            builder.setForecastViewerFrameID(R.id.idc_main_left_frame);
            builder.setPlacesViewerFrameID(R.id.idc_main_right_frame);
        }
        mViewer = builder.build();
        clearBackStack();
        savedPlace = mViewer.getPlaceViewer().restoreState();
    }

    private void initIViewInheritage(){
        mPresenter = null;
        mIsPresenterConnected = false;
        mIsUIReady = true;
    }

    public void showPopup(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void lookupOnMVPReady(IPresenter presenter){
        presenter.getListOfSavedPlaces();
        presenter.acquireForecastsForAllPlaces();
    }

    /**
     * ForecastViewer.IHolderInterface callback implementation, hands off calls to the
     * place forecast viewer
     */
    @Override
    public void onOtherDayButtonClicked() { mViewer.onOtherDayButtonClicked(); }
    @Override
    public void onOtherPlaceButtonClicked() {
        mViewer.onOtherPlaceButtonClicked();
    }
    @Override
    public void onDaySelected(int position) {
        mViewer.onDaySelected(position);
    }
    @Override
    public void onEmptyViewClicked() {
        mViewer.onEmptyViewClicked();
    }
    @Override
    public void onPlaceSelected(int viewPosition) {
        mViewer.onPlaceSelected(viewPosition);
        if (viewPosition != selectedPosition && null != savedPlace){
            IPlacesViewer pv = mViewer.getPlaceViewer();
            pv.saveState(pv.getPlaceByPosition(viewPosition));
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    /** Launch other activitu to add new places, not its's a test */
    @Override
    public void onNoPlacesUpdate() {
        toggleDynamicUI();
    }

    /**
     * Remove menu items we don't need in that UI type and change titles of others
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem toggleUI = menu.findItem(R.id.menu_main_toggle_ui_mode);
        toggleUI.setTitle(R.string.ids_menu_toggle_ui_dynamic);
        menu.removeItem(R.id.menu_main_add_new_place);
        menu.removeItem(R.id.menu_main_toggle_map);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_update_online:
                mViewer.reset();
                updateOnline();
                break;
            case R.id.menu_main_clear_all_places:
                mPresenter.removeAllPlaces();
                clearBackStack();
                break;
            case R.id.menu_main_add_predefined_places:
                addPredefinedPlaces();
                break;
            case R.id.menu_main_add_new_place:
                toggleDynamicUI();
                break;
            case R.id.menu_main_toggle_ui_mode:
                toggleDynamicUI();
                break;
            default:
        }
        return true;
    }
    /** Tell something else to add a new place (perhaps, GoogleMap)
     * Done:  add new screen with GoogleMap, where user can select place, see its name from
     * */
    void toggleDynamicUI() {
        Intent intent = new Intent(this, UIDynamicActivity.class);
        startActivity(intent);
        finish();
    }

    void addPredefinedPlaces(){
        showPopup(getResources().getString(R.string.ids_popup_predefined_add_predefined_places));
        Resources res = getResources();
        String name = "";
        double lat = 0;
        double lon = 0;
        /** add place 1 */
        name = res.getString(R.string.ids_predefined_place1_name);
        lat = Double.valueOf(res.getString(R.string.ids_predefined_place1_lat));
        lon = Double.valueOf(res.getString(R.string.ids_predefined_place1_lon));
        mPresenter.addNewPlace(new LocationData(lat, lon, name));

        /** add place 2 */
        name = res.getString(R.string.ids_predefined_place2_name);
        lat = Double.valueOf(res.getString(R.string.ids_predefined_place2_lat));
        lon = Double.valueOf(res.getString(R.string.ids_predefined_place2_lon));
        mPresenter.addNewPlace(new LocationData(lat, lon, name));

        /** add place 3 */
        name = res.getString(R.string.ids_predefined_place3_name);
        lat = Double.valueOf(res.getString(R.string.ids_predefined_place3_lat));
        lon = Double.valueOf(res.getString(R.string.ids_predefined_place3_lon));
        mPresenter.addNewPlace(new LocationData(lat, lon, name));

        /** add place 4 */
        name = res.getString(R.string.ids_predefined_place4_name);
        lat = Double.valueOf(res.getString(R.string.ids_predefined_place4_lat));
        lon = Double.valueOf(res.getString(R.string.ids_predefined_place4_lon));
        mPresenter.addNewPlace(new LocationData(lat, lon, name));

        mPresenter.acquireForecastsForAllPlaces();
    }

    void updateOnline(){
        mPresenter.setForceNetworkUpdate(true);
        mPresenter.acquireForecastsForAllPlaces();
        mUpdatingOnline = true;
    }

    /** Clear back stack from fragments left from previous session*/
    void clearBackStack(){
        FragmentManager fm = getFragmentManager();
        for (int i =0; i < fm.getBackStackEntryCount(); ++i){
            fm.popBackStack();
        }
    }

    boolean mUpdatingOnline;
    int mOnlineResponseCnt;
    int mNumOfPlaces;
    IViewerAndDataHolder mViewer;

    private IPresenter mPresenter;
    boolean mIsPresenterConnected;
    boolean mIsUIReady;

}

