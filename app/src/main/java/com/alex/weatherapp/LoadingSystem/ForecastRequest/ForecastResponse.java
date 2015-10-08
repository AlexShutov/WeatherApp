package com.alex.weatherapp.LoadingSystem.ForecastRequest;

import com.alex.weatherapp.LoadingSystem.IResponse;

/**
 * Created by Alex on 06.09.2015.
 */
public class ForecastResponse implements IResponse {
    public  ForecastResponse( ForecastData data) {
        mData = data;
        mIsSuccesseful = (data == null) ? false : true;
    }

    public ForecastData getForecastData(){
        return  mData;
    }
    public boolean isSuccess() {
        return  mIsSuccesseful;
    }

    private ForecastData mData;
    private boolean mIsSuccesseful;
}
