package com.alex.weatherapp.UIDynamic.FreePlaceGeoMonitor;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.UIDynamic.IViewingController;

/**
 * Created by Alex on 02.12.2015.
 */

/**
 * Integrates in MapViewer and represents app's behaviour when user selects some
 * place on map, and that place is not
 * the saved one nor is lying inside area of any saved place.
 * After user selects some place, acceptNewFreePlace has to be called by MapViewer
 * When user click saved place's marker or taps inside area around saved place, 'free place' mode
 * is no longer active, MapViewer calls onSavedPlaceSelected method.
 * Desired algorithm:
 *  a) user taps some area on map
 *  b) In response to a) default PlacePicker adds some temporary value with coordinates of
 *      tapped place and saves that place as the current one.
 *      We don't know forecast for that place, so IFreePlaceMonitor forces IViewing controller
 *      to clear current forecast. IFreePlaceMonitor knows about IViewing controller
 *      (the default controller - without map). That mean it has to be a part of AppHub.
 *  c) at the same time with b) some subsystem requests real name of that place by using
 *      Google inverse geolookup.
 *  d) IFreePlaceMonitor work with IPresenter, and IView, so it queries forecast for that free place
 *  e) If user taps different place, we repeat step b). When user taps very rapidly and phone has
 *      slow internet connection, received forecast and place name might be obsolete.
 *  f) Place name or forecast arrives (or both).
 *      f1) we've got place name. Force ICityPicker to change name to new name. But we still don't
 *      have forecast and it was queried under previous name (with coordinates). So, we must save
 *      previous name, new name and location of current place.
 *      f2). forecast has arrived. We validate forecast's place against place and forecast's place
 *          name against old and new name (if we have it), and if forecast valid,
 *          IFreePlaceMonitor tells IForecastViewer to display that forecast
 *   g) User selects some saved place. We clear temporary value inside ICityPicker, remove
 *      forecast from IForecastViewer, and mark IFreePlaceMonitor as inactive so all requested
 *      place names and forecast would take no effect. *
 */
public interface IFreePlaceMonitor {
    /**
     * IFreePlaceMonitor conceptually doesn't need to know about outer program layer, e.g.
     * IPresenter. AppHub must tie them together. All IFreePlaceMonitor need is be able to
     * say AppHub that he want to know forecast for recently selected place. AppHub, in turn,
     * must request it from IPresenter, if IPresenter is read, of course, and when LoadingSystem
     * got that forecast, AppHub hands it to that monitor.
     */
    interface IForecastOnlineRequester{
        void requestForecast(LocationData placeWeNeedForecastFor);
    }
    void setForecastRequesterLink(IForecastOnlineRequester requester);
    void acceptNewFreePlace(LocationData place);
    void acceptFreePlaceForecast(PlaceForecast forecast);
    void onSavedPlaceSelected();

    void onStart();
    void onStop();

    /**
     * IFreePlaceMonitor cooperate with IViewingController
     * @param viewingController
     */
    void assignViewingController(IViewingController viewingController);
}
