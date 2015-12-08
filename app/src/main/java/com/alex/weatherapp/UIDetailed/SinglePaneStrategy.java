package com.alex.weatherapp.UIDetailed;

import android.app.Activity;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIDetailed.PlacesViewer.IPlacesViewer;

import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 08.10.2015.
 */
public class SinglePaneStrategy implements IViewingStrategy {

    public SinglePaneStrategy(){
        mHolder = null;
        mForecastViewer = null;
        mPlacesViewer = null;
    }

    /**
     * Inherited from IPlacesViewer.IPlaceSelectedCallback
     */

    private boolean isPlaceListActive(){
        return mHolder.getCurrPlace() == null;
    }

    @Override
    public void onPlaceSelected(LocationData selectedPlace) {
        mHolder.setCurrPlace(selectedPlace);
        Forecast f = mHolder.getData().get(selectedPlace);
        mForecastViewer.showForecast(f);
    }

    @Override
    public void onNoPlaceUpdateRequested() {
        mHolder.getLink().onNoPlacesUpdate();
    }

    @Override
    public void onOtherPlaceButtonClicked() {
        mHolder.setCurrPlace(null);
        mPlacesViewer.invalidate();
    }

    @Override
    public void onOtherDayButtonClicked() {
        mForecastViewer.onOtherDayButtonClicked();
    }

    /**
     * Inherited from IViewingStrategy
     */

    @Override
    public void setHolder(IViewerAndDataHolder holder) {
        mHolder = holder;
        mForecastViewer = holder.getForecastViewer();
        mPlacesViewer = holder.getPlaceViewer();
    }

    @Override
    public void showPlaces(List<LocationData> places) {
        mPlacesViewer.showPlaceList(places);
    }

    /** Compared to dual-pane layout, here we don't need to draw new view for place list,
     * just udate its data. For that, enter silent mode, update data and disable silent mode.
     * @param f
     */
    @Override
    public void showPlaceForecast(PlaceForecast f) {
        LocationData place = f.getPlace();
        if (f.getForecast() == null){
            mForecastViewer.showForecast(null);
        }
        Map<LocationData,Forecast> data = mHolder.getData();
        LocationData currPlace = mHolder.getCurrPlace();
        if (currPlace != null && currPlace == place){
            mForecastViewer.showForecast(f.getForecast());
        }
        data.remove(place);
        /** if current place is null, we are saw list of places, update it, otherwise-
         * forecast view is active */
        if (currPlace == null){
            mPlacesViewer.processIncomingResponse(place, true);
        }else {
            mPlacesViewer.turnOnSilentMode(true);
            mPlacesViewer.processIncomingResponse(place, true);
            mPlacesViewer.turnOnSilentMode(false);
        }
        data.put(place, f.getForecast());
    }

    @Override
    public void showPlacesForecasts(List<PlaceForecast> forecasts) {
        for (PlaceForecast f : forecasts){
            showPlaceForecast(f);
        }
    }

    /** Inherited from ILinkToHolderActivity (the next two methods) */
    @Override
    public Activity getActivity() {
        return mHolder.getLink().getActivity();
    }

    @Override
    public void onNoPlacesUpdate() {

    }





    @Override
    public void onDaySelected(int position) {
        mForecastViewer.onDaySelected(position);
    }

    @Override
    public void onPlaceSelected(int viewPosition) {
        mPlacesViewer.onPlaceSelected(viewPosition);
    }

    @Override
    public void onEmptyViewClicked() {
        mPlacesViewer.onEmptyViewClicked();
    }

    IViewerAndDataHolder mHolder;
    IPlacesViewer mPlacesViewer;
    IForecastViewer mForecastViewer;
}
