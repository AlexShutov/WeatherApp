package com.alex.weatherapp.UIDetailed.PlacesViewer;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

/**
 * Created by Alex on 06.10.2015.
 */

/** Contains place and request state. It is used by PlaceNameAdapter and PlaceNameFragment as
 * its object kind
 */
public class RowData {
    public RowData(LocationData place, PlaceUpdateState state){
        mPlace = place;
        mState = state;
    }
    public LocationData getPlace(){ return mPlace;}
    public PlaceUpdateState getState(){ return mState;}
    LocationData mPlace;
    PlaceUpdateState mState;
}