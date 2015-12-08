package com.alex.weatherapp.UIDetailed;

import android.app.Activity;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.ForecastViewer;
import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIDetailed.PlacesViewer.IPlacesViewer;
import com.alex.weatherapp.UIDetailed.PlacesViewer.PlacesViewer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Alex on 07.10.2015.
 */
public class ViewerAndDataHolder implements IViewerAndDataHolder {


    public static class Builder implements IHolderBuilder {

        public Builder(){
            /** init with empty default values */
            mDisplayingMode = DisplayingMode.MODE_SINGLE_FRAME;
            mLink = null;
            mInst = null;
            mLayoutFrameID = 0;
            mPlacesViewerFrameID = 0;
            mForecastViewerFrameID = 0;
        }

        @Override
        public void setLinkToHoldingActivity(ILinkToHolderActivity linkToActivity) {
            mLink = linkToActivity;
        }

        @Override
        public void setDisplayingMode(DisplayingMode mode) {
            mDisplayingMode = mode;
        }

        @Override
        public void setForecastViewerFrameID(int frameLayoutID) {
            mForecastViewerFrameID = frameLayoutID;
        }

        @Override
        public void setPlacesViewerFrameID(int frameLayoutID) {
            mPlacesViewerFrameID = frameLayoutID;
        }

        @Override
        public void setFrameID(int frameLayoutID) {
            mLayoutFrameID = frameLayoutID;
        }

        @Override
        public ViewerAndDataHolder build() throws IllegalStateException {
            switch (mDisplayingMode){
                case MODE_SINGLE_FRAME:
                    if (mLayoutFrameID == 0){
                        throw new IllegalStateException("Single frame mode: frame isn't set");
                    }
                    buildSingleFrameInstance();
                    break;
                case MODE_TWO_FRAMES:
                    if (mForecastViewerFrameID == 0 || mPlacesViewerFrameID == 0){
                        throw new IllegalStateException("Two frame mode: some frame isn't set");
                    }
                    buildTwoFrameInstance();
                    break;
            }
            return mInst;
        }

        private void buildTwoFrameInstance(){
            mInst = new ViewerAndDataHolder(null);

            mInst.mActivityLink = mLink;
            mInst.mDataReceived = new TreeMap<>();
            mInst.mStrategy = new DualPaneStrategy();
            mInst.mPlacesViewer = new PlacesViewer(mLink.getActivity());
            mInst.mPlacesViewer.setFrameResourceID(mPlacesViewerFrameID);
            mInst.mPlacesViewer.setSelectedCallback(mInst.mStrategy);

            mInst.mForecastViewer = new ForecastViewer(mLink.getActivity(), mForecastViewerFrameID);
            mInst.mForecastViewer.setHoldingActivity(mLink.getActivity());
            mInst.mForecastViewer.setIsOtherPlaceButtonActive(false);
            mInst.mCurrPlace = null;
            mInst.mStrategy.setHolder(mInst);
        }

        private void buildSingleFrameInstance(){
            mInst = new ViewerAndDataHolder(null);
            mInst.mActivityLink = mLink;
            mInst.mDataReceived = new TreeMap<>();
            mInst.mStrategy = new SinglePaneStrategy();

            mInst.mPlacesViewer = new PlacesViewer(mLink.getActivity());
            mInst.mPlacesViewer.setFrameResourceID(mLayoutFrameID);
            mInst.mPlacesViewer.setSelectedCallback(mInst.mStrategy);

            mInst.mForecastViewer = new ForecastViewer(mLink.getActivity(),
                    mLayoutFrameID);
            mInst.mForecastViewer.setHoldingActivity(mLink.getActivity());
            mInst.mForecastViewer.setOnOtherBtnCallback(mInst.mStrategy);
            mInst.mCurrPlace = null;

            mInst.mStrategy.setHolder(mInst);
        }


        private ILinkToHolderActivity mLink;
        private DisplayingMode mDisplayingMode;
        private int mForecastViewerFrameID;
        private int mPlacesViewerFrameID;
        private int mLayoutFrameID;
        private ViewerAndDataHolder mInst;
    }

    /**
     * Use builder for instantiating
     * @param link
     */
    private ViewerAndDataHolder(ILinkToHolderActivity link){


        /**
        mLink = link;
        mDataReceived = new TreeMap<>();
        mStrategy = new SinglePaneStrategy();

        mPlacesViewer = new PlacesViewer(link.getActivity());
        mPlacesViewer.setFrameResourceID(R.id.idc_main_details);
        mPlacesViewer.setSelectedCallback(mStrategy);

        mForecastViewer = new ForecastViewer(link.getActivity(), R.id.idc_main_details);
        mForecastViewer.setHoldingActivity(link.getActivity());
        mForecastViewer.setOnOtherBtnCallback(mStrategy);
        mCurrPlace = null;

        mStrategy.setHolder(this);
         */
    }

    @Override
    public LocationData getCurrPlace() {  return mCurrPlace; }

    @Override
    public void setCurrPlace(LocationData place) {mCurrPlace = place; }


    /**
     * When there are no saved places, place viewer ask to add them on touch.
     * It calls activity, activity redirects it in here, holder, in turn, hands call back to
     * activity but through other interface. For compatibility reasons, this holder has the next
     * after this onNoPlaceUpdateRequested() method, which, in fact, does nothing.
     */
    @Override
    public void onNoPlacesUpdate() {
        mActivityLink.onNoPlacesUpdate();
    }


    /** Inherited from IViewerAndDataHolder */
    @Override
    public void reset() {
        mDataReceived.clear();
        mPlacesViewer.reset();
    }

    @Override
    public IPlacesViewer getPlaceViewer() {
        return mPlacesViewer;
    }

    @Override
    public IForecastViewer getForecastViewer() {
        return mForecastViewer;
    }

    @Override
    public void showPlaces(List<LocationData> places) {
        mStrategy.showPlaces(places);
    }

    @Override
    public void showPlaceForecast(PlaceForecast f) {
        mStrategy.showPlaceForecast(f);
    }

    @Override
    public void showPlacesForecasts(List<PlaceForecast> forecasts) {
        mStrategy.showPlacesForecasts(forecasts);
    }

    /** Inherited from ILinkToHolderActivity, redirects call to according viewer   */
    @Override
    public Activity getActivity() {
        return mActivityLink.getActivity();
    }

    @Override
    public ILinkToHolderActivity getLink() {
        return mActivityLink;
    }

    @Override
    public Map<LocationData, Forecast> getData() {
        return mDataReceived;
    }

    @Override
    public void onOtherDayButtonClicked() {
        mStrategy.onOtherDayButtonClicked();
    }

    @Override
    public void onOtherPlaceButtonClicked() {
        mStrategy.onOtherPlaceButtonClicked();
    }

    @Override
    public void onDaySelected(int position) {
        mStrategy.onDaySelected(position);
    }

    @Override
    public void onPlaceSelected(int viewPosition) {
        mStrategy.onPlaceSelected(viewPosition);
    }

    @Override
    public void onEmptyViewClicked() {
        mStrategy.onEmptyViewClicked();
    }


    private Map<LocationData, Forecast> mDataReceived;
    private IForecastViewer mForecastViewer;
    private IPlacesViewer mPlacesViewer;
    LocationData mCurrPlace;
    private ILinkToHolderActivity mActivityLink;
    IViewingStrategy mStrategy;
}
