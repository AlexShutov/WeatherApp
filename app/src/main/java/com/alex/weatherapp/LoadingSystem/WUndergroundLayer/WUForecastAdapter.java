package com.alex.weatherapp.LoadingSystem.WUndergroundLayer;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastData;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastResponse;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.IResultDecorator;

/**
 * Created by Alex on 07.09.2015.
 */

/**
 * Performs result transformation from WUnderground format to inner program format
 */

public class WUForecastAdapter implements IResultDecorator {

    @Override
    public IResponse decorate(IResponse response) throws IllegalArgumentException, IllegalStateException {
        if (!(response instanceof ForecastResponse)) {
            throw new IllegalArgumentException("Wrong response type, ForecastResponse instance is expected");
        }
        ForecastData incomingData = ((ForecastResponse) response).getForecastData();
        /* handle only WUForecast data, silently ignore it otherwise */
        // TODO someday, when there'll be not just WU
        /*
        if (! (incomingData instanceof WUForecastData)) {
            return  response;
        }*/
        WUForecastData d = (WUForecastData) incomingData;
        ForecastData transformedData = null;
        transformedData = Forecast.convertForecastFromWUndergroundData(d);
        return  new ForecastResponse(transformedData);
    }
}
