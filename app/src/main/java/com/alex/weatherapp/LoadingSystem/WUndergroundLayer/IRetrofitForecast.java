package com.alex.weatherapp.LoadingSystem.WUndergroundLayer;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Alex on 06.09.2015.
 */

/**
 * @Param(request_type) the type of forecast request- for 3 or 10 days to come,
 * 'forecast' and 'forecast10day' accordingly
 * @Param(lat) latitude
 * @Param(lon) longitude
 */
public interface IRetrofitForecast {
    @GET("/api/{app_id}/{request_type}/q/{lat},{lon}.json")
    Call<WUForecastData> getForecast(@Path("app_id") String appID,
                                @Path("request_type") String request_type,
                                @Path("lat") double lat,
                                @Path("lon") double lon);
}


/**
 *
 */
