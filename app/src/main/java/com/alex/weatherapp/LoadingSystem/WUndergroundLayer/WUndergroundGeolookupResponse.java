package com.alex.weatherapp.LoadingSystem.WUndergroundLayer;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupResponse;

/**
 * Created by Alex on 06.09.2015.
 */
public class WUndergroundGeolookupResponse extends GeolookupResponse {
    public WUndergroundGeolookupResponse() {
        super(new WUndergroundGeolookupData());
    }

}
