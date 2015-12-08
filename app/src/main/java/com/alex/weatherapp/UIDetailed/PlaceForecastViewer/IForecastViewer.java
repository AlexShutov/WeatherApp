package com.alex.weatherapp.UIDetailed.PlaceForecastViewer;

import android.app.Activity;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;

/**
 * Created by Alex on 05.10.2015.
 */
public interface IForecastViewer extends ForecastDetailsFragment.IDayFragmentHolderCallback,
        DaysDetailsFragment.IDaysDetailsFragmentHolder {

    /**
     * ForecastViewer uses this interface, representing callback from according
     * fragments. Holding activity must implement it for ForecastViewer being able
     * to plug in it. In that implementation activity must redirect calls to the instance of
     * ForecastViewer.
     */
    interface IHolderInterface extends
            ForecastDetailsFragment.IDayFragmentHolderCallback,
            DaysDetailsFragment.IDaysDetailsFragmentHolder {
    }

    interface IOtherPlaceButonCallback {
        void onOtherPlaceButtonClicked();
    }
    void setOnOtherBtnCallback(IOtherPlaceButonCallback cb);
    void setIsOtherPlaceButtonActive(boolean isActive);

    void setHoldingActivity(Activity activity);
    void showDayForecast(Forecast.DayForecast dayForecastorecast);
    void showForecast(Forecast forecast);
}
