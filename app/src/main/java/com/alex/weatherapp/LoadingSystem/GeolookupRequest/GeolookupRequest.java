package com.alex.weatherapp.LoadingSystem.GeolookupRequest;

import com.alex.weatherapp.LoadingSystem.RequestAbstract;

/**
 * Created by Alex on 05.09.2015.
 */
public class GeolookupRequest extends GeolookupData implements RequestAbstract {

    public GeolookupRequest(double lat, double lon) {
        super(lat, lon);
    }

};
