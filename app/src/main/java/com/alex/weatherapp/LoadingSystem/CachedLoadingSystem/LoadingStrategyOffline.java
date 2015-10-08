package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

import android.os.Handler;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastRequest;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastResponse;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupData;
import com.alex.weatherapp.LoadingSystem.LocalStorage.ILocalStorageRequests;

import java.util.Date;

/**
 * Created by Alex on 22.09.2015.
 */
public class LoadingStrategyOffline implements ILoadingStrategy {

    public void setLoadingFacility(ILoadingFacility facility) { mFacility = facility; }

    /**
     * This method is the same, as in CachedStrategy. We don't need to save state in the list,
     * but it is done by IFacility.
     * @param reqState
     */
    @Override
    public void initialRequest(StateOfExecution reqState) {

        /* For now process only foreasts and location requests, this is unreachable code, because
         * interceptor will not acccept Geolookup request */
        if (reqState.requestType == StateOfExecution.RequestType.Geolookup) {
            reqState.isDone = true;
            mFacility.updateRequestState(reqState);
            throw new RuntimeException("Fatal error, Wrong request type");
        }

        if (reqState.requestType == StateOfExecution.RequestType.LocationRequest) {
            mFacility.processLocationRequest(reqState);
            reqState.isDone = true;
        }

        if (reqState.requestType == StateOfExecution.RequestType.ForecastRequest) {
            ILocalStorageRequests localStorage = mFacility.getLocalStorage();
            ForecastRequest fReq = (ForecastRequest) reqState.request;
            /** Ignore request if flag OnlineNoCache is set, bcause we're offline */
            if (fReq.getOnlineNoCache()){
                reqState.isDone = true;
                mFacility.updateRequestState(reqState);
            }
            GeolookupData coord = new GeolookupData(fReq.getLat(), fReq.getLon());
            /* delete obsolete forecasts for all days before today */
            localStorage.deleteObsoleteForecasts(new Date());
            Forecast cachedForecast = localStorage.getRecordsByCoordinates(coord);

            reqState.localResponse = new ForecastResponse(cachedForecast);
        }
        reqState.isHandledLocally = true;
        mFacility.updateRequestState(reqState);
            /* Notify about   */
        this.onLocalStorageResponse(reqState);
    }

    @Override
    public void registerLoadingFacility(ILoadingFacility facility) {
        mFacility = facility;
    }

    @Override
    public void onLocalStorageResponse(final StateOfExecution state) {
        if (state.callback == null) return;
        Handler callerThreaH = mFacility.getCallbackThreadHandler();
        callerThreaH.post(new Runnable() {
            @Override
            public void run() {
                state.callback.onResult(state.localResponse);
            }
        });
    }

    @Override
    public void onNetworkResponse(StateOfExecution state) {

    }

    private ILoadingFacility mFacility;
}
