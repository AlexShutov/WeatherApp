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
public class DualPaneStrategy implements IViewingStrategy {

    public DualPaneStrategy(){
    }

    @Override
    public void setHolder(IViewerAndDataHolder holder) {
        mHolder = holder;
        mPlacesViewer = mHolder.getPlaceViewer();
        mForecastViewer = mHolder.getForecastViewer();
    }

    /**
     * Inherited from IPlacesViewer.IPlaceSelectedCallback and
     * IForecastViewer.IOtherPlaceButonCallback
     */
    @Override
    public void onPlaceSelected(LocationData selectedPlace) {
        Forecast f = mHolder.getData().get(selectedPlace);
        mForecastViewer.showForecast(f);
    }


    /**
     * Hand request to linked activity
     */
    @Override
    public void onNoPlaceUpdateRequested() {
        mHolder.getLink().onNoPlacesUpdate();
    }


    /**
     * In dual-frame mode show message in forecast viewer that there are no data if size
     * of array is 0
     * @param places list of places to display
     */
    @Override
    public void showPlaces(List<LocationData> places) {
        if (places.size() == 0){
            mForecastViewer.showForecast(null);
        }
        mPlacesViewer.showPlaceList(places);
    }

    /**
     * Inherited from IViewingStrategy
     */

    @Override
    public void showPlaceForecast(PlaceForecast f) {
        if (f.getForecast() == null){
            mForecastViewer.showForecast(null);
        }
        LocationData place = f.getPlace();
        Map<LocationData,Forecast> data = mHolder.getData();
        LocationData currPlace = mHolder.getCurrPlace();
        /** if user now sees older version of that forecast */
        if (currPlace == null ||
                (currPlace != null && currPlace == place)){
            mForecastViewer.showForecast(f.getForecast());
            data.remove(place);
        }
        mPlacesViewer.processIncomingResponse(place, true);
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
    public void onOtherDayButtonClicked() {
        mForecastViewer.onOtherDayButtonClicked();
    }

    @Override
    public void onOtherPlaceButtonClicked() {
        mForecastViewer.onOtherPlaceButtonClicked();
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
