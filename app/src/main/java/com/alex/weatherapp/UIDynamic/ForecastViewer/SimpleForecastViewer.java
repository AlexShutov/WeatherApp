package com.alex.weatherapp.UIDynamic.ForecastViewer;

import android.app.Activity;
import android.widget.FrameLayout;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.Utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 23.11.2015.
 */
public class SimpleForecastViewer implements IForecastViewer {

    public SimpleForecastViewer(int[] framesResourceIDs,
                                Activity activity){
        mFrameResourcesIDs = new ArrayList<>();
        for (int rId: framesResourceIDs){
            mFrameResourcesIDs.add(rId);
        }
        setHoldingActivity(activity);
    }


    @Override
    public void setOnOtherBtnCallback(IOtherPlaceButonCallback cb) {
        Logger.e("setOnOtherBtnCallback() is called, but short forecast viewer is already" +
        "showing forecasts for all days");
    }

    @Override
    public void setIsOtherPlaceButtonActive(boolean isActive) {
        Logger.e("setIsOtherPlaceButtonActive(" + isActive + ") but place gets selected by " +
        "different ui element (spinner)");
    }

    @Override
    public void onOtherDayButtonClicked() {
        Logger.e("onOtherDayButtonClicked(..) has fired; short form viewer gives short forecast"+
                "for all days and can't switch between those");
    }

    @Override
    public void onOtherPlaceButtonClicked() {
        Logger.e("onOtherPlaceButtonClicked() is called, but this viewer doesn't support it");
    }

    @Override
    public void onDaySelected(int position) {
        Logger.e("onDaySelected(" + position +
                ") is called, short summary forecast viewer doesn't support it");
    }

    @Override
    public void setHoldingActivity(Activity activity) {
        if (null == activity){
            throw new RuntimeException("SimpleForecastViewer: Activity reference is null");
        }
        mActivity = activity;
    }

    /** here we show day forecast in first frame and clear the rest
     *
     * @param dayForecastorecast
     */
    @Override
    public void showDayForecast(Forecast.DayForecast dayForecastorecast) {
        if (mFrameResourcesIDs.isEmpty()){
            Logger.w("there are no availible place for showing that forecast");
            return;
        }
        int holder = mFrameResourcesIDs.get(0);
        DayForecastSimpleViewer.showDayForecast(dayForecastorecast, true, holder, mActivity);
    }

    /**
     * show forecast and clear frames for day forecasts if
     * @param forecast
     */
    @Override
    public void showForecast(Forecast forecast) {
        int nForecasts = forecast.mDayForecasts.size();
        int nFrames = mFrameResourcesIDs.size();
        int i = 0;
        if (nForecasts == 0){
            for (; i< nFrames; ++i){
                int frameID = mFrameResourcesIDs.get(i);
                FrameLayout fl = (FrameLayout) mActivity.findViewById(frameID);
                fl.removeAllViews();
            }
            i = 0;
        }
        while (i < nForecasts && i < nFrames) {
            boolean isBig = i == 0;
            int frameID = mFrameResourcesIDs.get(i);
            Forecast.DayForecast df = forecast.mDayForecasts.get(i);
            DayForecastSimpleViewer.showDayForecast(df, isBig, frameID, mActivity);
            i++;
        }
        Logger.i("Forecast is shown for "+ Math.min(nForecasts, nFrames) + " days");
    }

    private Activity mActivity;
    private List<Integer> mFrameResourcesIDs;
}
