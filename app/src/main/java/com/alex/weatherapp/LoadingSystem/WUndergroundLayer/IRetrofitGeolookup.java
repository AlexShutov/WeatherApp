package com.alex.weatherapp.LoadingSystem.WUndergroundLayer;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Alex on 05.09.2015.
 */

/**
 * @Param(lat) latitude
 * @Param(lon) longitude
 */
public interface IRetrofitGeolookup {
    @GET("/api/{app_id}/geolookup/q/{lat},{lon}.json")
    Call<WUndergroundGeolookupData> getGeolookupData(@Path("app_id") String appID,
                                                     @Path("lat") double lat,
                                                     @Path("lon") double lon);
}
