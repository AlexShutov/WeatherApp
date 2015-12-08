package com.alex.weatherapp.UIDetailed.PlaceForecastViewer;

import android.app.Activity;
import android.app.FragmentManager;
import android.util.Log;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.WeatherApplication;

/**
 * Created by Alex on 05.10.2015.
 */
public class ForecastViewer implements IForecastViewer {

    public ForecastViewer(){
        mHolder = null;
        mForecastToShow = new Forecast();
        mFrameResourceID = 0;
        mIsOtherPlaceButtonActive = true;
        mIsOtherDayButtonActive = true;
        mOtherBtnCallback = null;
    }
    public ForecastViewer(Activity activity, int frameResourceID){
        mHolder = activity;
        mForecastToShow =  new Forecast();
        mFrameResourceID = frameResourceID;
        mIsOtherPlaceButtonActive = true;
        mIsOtherDayButtonActive = true;
        mOtherBtnCallback = null;
    }

    public void setFrameResourceID(int frameID){
        mFrameResourceID = frameID;
    }

    @Override
    public void setHoldingActivity(Activity activity) {
        mHolder = activity;
    }

    @Override
    public void showDayForecast(Forecast.DayForecast forecast) {
        if (mFrameResourceID == 0) return;
        Log.d("Viewing strategy", "void showDayForecast(Forecast.DayForecast forecast)");
        FragmentManager fm = mHolder.getFragmentManager();
        ForecastDetailsFragment fFragment =
                ForecastDetailsFragment.newInstance(forecast,mIsOtherDayButtonActive,
                        mIsOtherPlaceButtonActive);
        fm.beginTransaction().replace(mFrameResourceID, fFragment)
                .addToBackStack(WeatherApplication.BACK_STACK_TAG).commit();
    }

    /**
     * In this strategy the forecast for today is shown first, because usually I want to know
     * today's weather. If user want to see forecast for another day, he presses the button at the
     * bottom, it notifies activity by means of ForecastDetailsFragment.IDayFragmentHolderCallback,
     * which, in turn, replaces the current fragment with a list of days availible. It is very
     * convenient, because every action is undoable due to fragment's back stack support.
     *
     * @param forecast
     */
    @Override
    public void showForecast(Forecast forecast) {
        if (forecast == null) {
            showNotFoundMessage();
            return;
        }
        mForecastToShow = forecast;
        if (mForecastToShow.mDayForecasts.size() != 0) {
            Forecast.DayForecast df = mForecastToShow.mDayForecasts.get(0);
            showDayForecast(df);
        }
    }

    @Override
    public void onOtherDayButtonClicked() {
        if (mHolder != null) {
            if (mFrameResourceID == 0) return;
            /** replace the existing fragment with the one, showing list of days are availible */
            FragmentManager fm = mHolder.getFragmentManager();
            DaysDetailsFragment df = DaysDetailsFragment.newInstance(mForecastToShow);
            fm.beginTransaction().replace(mFrameResourceID, df)
                    .addToBackStack(WeatherApplication.BACK_STACK_TAG).commit();
        }
    }
    @Override
    public void onOtherPlaceButtonClicked() {
        if (mOtherBtnCallback != null) {
            mOtherBtnCallback.onOtherPlaceButtonClicked();
        }
    }
    @Override
    public void onDaySelected(int position) {
        Forecast.DayForecast newDay = mForecastToShow.mDayForecasts.get(position);
        showDayForecast(newDay);
    }

    /** Show empty fragment with 'not found message' */
    void showNotFoundMessage(){
        FragmentManager fm = mHolder.getFragmentManager();
        ForecastNotFoundFragment notFound = new ForecastNotFoundFragment();
        fm.beginTransaction().replace(mFrameResourceID, notFound).commit();
    }

    @Override
    public void setOnOtherBtnCallback(IOtherPlaceButonCallback cb) {
        mOtherBtnCallback = cb;
    }
    @Override
    public void setIsOtherPlaceButtonActive(boolean isActive){
        mIsOtherPlaceButtonActive = isActive;
    }


    public void setIsOtherDayButtonActive(boolean isActive){
        mIsOtherDayButtonActive = isActive;
    }

    private Activity mHolder;
    private Forecast mForecastToShow;
    private int mFrameResourceID;
    /** set it in call when other place button is clicked (if set) */
    private IOtherPlaceButonCallback mOtherBtnCallback;
    /** if set, Other place button will appear next time, when day forecast
     * fragment is displayed. True by default */
    private boolean mIsOtherPlaceButtonActive;
    private boolean mIsOtherDayButtonActive;
}
