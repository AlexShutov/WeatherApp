package com.alex.weatherapp.UIv2.FreePlaceGeoMonitor;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.NetworkStateListener.INetStateListenerFeedback;
import com.alex.weatherapp.LoadingSystem.NetworkStateListener.NetworkStateListener;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.LocationAPI.GoogleLibFrame;
import com.alex.weatherapp.LocationAPI.InvGeoFeature;
import com.alex.weatherapp.LocationAPI.LibFeature;
import com.alex.weatherapp.UIv2.CityPicker.ICityPicker;
import com.alex.weatherapp.UIv2.IViewingController;
import com.alex.weatherapp.Utils.Logger;

/**
 * Created by Alex on 03.12.2015.
 */
public class GeoFreePlaceMonitor implements IFreePlaceMonitor,
        InvGeoFeature.IUserInvGeoCallback {

    public GeoFreePlaceMonitor(Context c){
        context = c;
        isUpdateRequested = false;
        connectionStateListener = new NetworkStateListener(c);
        connectionStateListener.setFeedback(connectionStateChangedFeedback);
        /** Init LocationAPI */
        locationAPI = new GoogleLibFrame(c.getApplicationContext());
        locationAPI.setInvGeoCompletionCallback(this);
        selectedFreePlace = null;
        modifiedFreePlace = null;
        lastForecastForFreePlace = null;
    }

    @Override
    public void onStart() {
        locationAPI.onStart();
        connectionStateListener.startListening();
        connectionStateListener.forceStateChecking();
    }

    @Override
    public void onStop() {
        removeTempPlaceFromPicker();
        viewingController.getAssignedForecastViewer().showForecast(new Forecast());
        connectionStateListener.stopListening();
        locationAPI.onStop();
    }

    @Override
    public void setForecastRequesterLink(IForecastOnlineRequester requester) {
        forecastRequester = requester;
    }

    /** If process of getting forecast hasn't been so long that user selected another place,
     * show that forecast by using IViewingController
     * @param forecast
     */
    @Override
    public void acceptFreePlaceForecast(PlaceForecast forecast) {
        LocationData place = forecast.getPlace();
        if (place.equals(sourceTempPlace)){
            viewingController.getAssignedForecastViewer().showForecast(forecast.getForecast());
        }

    }

    /** Inherited from InvGeoFeature.IUserInvGeoCallback - inverse geolookup complete callback */
    @Override
    public void onNoResultFound(LocationData place) {
        Logger.i("LocationAPI: No result found for place: " + place.getPlaceName());
    }
    @Override
    public void onTaskCompleted(LibFeature.LocationResultData data) {
        /** Process of getting place name may take too long */
        if (null == selectedFreePlace || !isUpdateRequested){
            return;
        }
        InvGeoFeature.InvGeoResultData result = (InvGeoFeature.InvGeoResultData) data;
        String placeNameSuggestion = result.getNameSuggestions().get(0);
        removeTempPlaceFromPicker();
        modifiedFreePlace = new LocationData(selectedFreePlace);
        modifiedFreePlace.setmPlaceName(placeNameSuggestion);
        sourceTempPlace.setmPlaceName(placeNameSuggestion);
        handleFinalFreePlace(modifiedFreePlace);
        /** at this time LocationApi has returned real place name and source place name had been
         * altered, so we can safely request forecast for that place. Also, this code is reachable
         * only if we have network connection, and it is possible to get forecast
          */
        if (null != forecastRequester){
            forecastRequester.requestForecast(sourceTempPlace);
        }
    }
    @Override
    public void onError(String errorMessage) {
        Logger.e("IGeoMonitor, error during getting real place name has occured: " +
                errorMessage);
    }

    /**
     * If we have no internet connection, we process free place as it is - with name consisting of
     * place's coordinates. But, if there are connection, we wait until LocationAPI provide
     * us place's name based on its coordinates and then we process place with new name.
     * @param place
     */
    @Override
    public void acceptNewFreePlace(LocationData place) {
        if (selectedFreePlace != null && place.equals(selectedFreePlace)) return;
        sourceTempPlace = place;
        //Toast.makeText(context, "Free place is selected", Toast.LENGTH_SHORT).show();
        selectedFreePlace = new LocationData(place);
        if (!isOnline){
            isUpdateRequested = false;
            removeTempPlaceFromPicker();
            modifiedFreePlace = new LocationData(selectedFreePlace);
            handleFinalFreePlace(selectedFreePlace);
            return;
        }
        InvGeoFeature.InvGeoRequestBuilder rb = locationAPI.createInvGeoRequestBuilder();
        rb.setPlaceForHandling(selectedFreePlace);
        Intent request = rb.createRequest();
        locationAPI.processRequest(request);
        isUpdateRequested = true;
    }

    @Override
    public void onSavedPlaceSelected() {
        Toast.makeText(context, "Saved place were selected",
                Toast.LENGTH_SHORT).show();
        isUpdateRequested = false;
        removeTempPlaceFromPicker();
    }

    @Override
    public void assignViewingController(IViewingController viewingController) {
        this.viewingController = viewingController;
    }

    /** Monitor calls this method when he finally figured out free place's name */
    private void handleFinalFreePlace(LocationData freePlace){
        //Toast.makeText(context, "Handling free place: " + freePlace.getPlaceName(), Toast.LENGTH_SHORT).show();
        if (null != viewingController){
            ICityPicker picker = viewingController.getAssignedPicker();
            picker.disableNextSelectionCallbackFiring(2, freePlace);
            picker.addCity(freePlace);
            viewingController.getAssignedForecastViewer().showForecast(new Forecast());
        }
    }
    private void removeTempPlaceFromPicker(){
        if (null != modifiedFreePlace) {
            viewingController.getAssignedPicker().disableNextSelectionCallbackFiring(2, null);
            viewingController.getAssignedPicker().removeCity(modifiedFreePlace);
        }
      //  selectedFreePlace = null;
        modifiedFreePlace = null;
    }


    private boolean isUpdateRequested;
    private boolean isOnline;

    private Forecast lastForecastForFreePlace;

    /** reference to selected place, is user for changing place name in the rest of program,
     * so if user add this place, he or she will se its actual name, nor coordinates.
     */
    private LocationData sourceTempPlace;
    /** stores local copy of temporarily selected place */
    private LocationData selectedFreePlace;
    /** Modified free place have the same coordinates as selectedFreePlace, but its name is altered
     * by inverse geolookup if device have Internet connection. If there are no connection, they
     * are the same, but modifiedFreePlace is used on IViewingContoller's side for altering UI.
     */
    private LocationData modifiedFreePlace;

    private Context context;
    private NetworkStateListener connectionStateListener;
    private GoogleLibFrame locationAPI;
    private IViewingController viewingController;
    private IForecastOnlineRequester forecastRequester;
    private INetStateListenerFeedback connectionStateChangedFeedback = new INetStateListenerFeedback() {
        @Override
        public void onOffline() {
            isOnline = false;
        }
        @Override
        public void onOnline() {
            isOnline = true;
        }
        @Override
        public void onWiFiAvailible() {
        }
        @Override
        public void onCellularAvailible() {
        }
    };
}
