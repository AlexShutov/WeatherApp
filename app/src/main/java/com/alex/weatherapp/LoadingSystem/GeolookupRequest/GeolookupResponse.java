package com.alex.weatherapp.LoadingSystem.GeolookupRequest;

import com.alex.weatherapp.LoadingSystem.IResponse;

/**
 * Created by Alex on 05.09.2015.
 */
public class GeolookupResponse implements IResponse {
    public  GeolookupResponse(GeolookupData data) {
        mData = data;
        mIsSuccesseful = (data == null) ? false : true;
    }

    public GeolookupData getGeolookupData(){
        return  mData;
    }
    public boolean isSuccess() {
        return  mIsSuccesseful;
    }

    private GeolookupData mData;
    private boolean mIsSuccesseful;
}
