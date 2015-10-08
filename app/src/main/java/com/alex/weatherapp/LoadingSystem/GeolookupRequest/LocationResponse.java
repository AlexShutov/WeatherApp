package com.alex.weatherapp.LoadingSystem.GeolookupRequest;

import com.alex.weatherapp.LoadingSystem.IResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 22.09.2015.
 */
public class LocationResponse implements IResponse {

    public LocationResponse(List<LocationData> loc) {
        mData = loc;
        if (loc == null) {
            mData = new ArrayList<>();
        }
        mIsSuccessefull = (loc == null)? false : true;
    }

    public boolean isSuccesseful() { return  mIsSuccessefull;}
    public List<LocationData> getLocations() {return mData;}
    private List<LocationData> mData;
    private boolean mIsSuccessefull;
}
