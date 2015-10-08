package com.alex.weatherapp.LoadingSystem.GeolookupRequest;

/**
 * Created by Alex on 22.09.2015.
 */

import com.alex.weatherapp.LoadingSystem.RequestAbstract;

/**
 * The same as geolookup request, but of another type. Retrives name for a given
 * coordinates, if there any
 */
public class LocationRequest implements RequestAbstract {

    /**
     * Defines a kind of location request (see db)
     */
    public enum RequestType {
        GET_ALL_PLACES,
        GET_ONE_PLACE_BY_COORD,
        ADD_NEW_PLACE,
        REMOVE_PLACE_BY_COORD,
        REMOVE_ALL_PLACES
    };
    public LocationRequest(LocationData d, RequestType requestType) {
        setLocationData(d);
        mRequestType = requestType;
    }

    public LocationData getLocationData() { return  mData;}
    public void setLocationData(LocationData d){
        if (d != null) {
            mData = d;
        } else {
            mData = new LocationData(0, 0, "");
        }
    }
    public RequestType getRequestType() { return  mRequestType;}


    private RequestType mRequestType;
    private LocationData mData;


}
