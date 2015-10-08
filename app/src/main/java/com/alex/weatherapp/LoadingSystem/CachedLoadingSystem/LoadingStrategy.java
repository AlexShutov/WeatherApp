package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

import android.os.Handler;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastRequest;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastResponse;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupData;
import com.alex.weatherapp.LoadingSystem.LocalStorage.ILocalStorageRequests;

import java.util.Date;

/**
 * Created by Alex on 17.09.2015.
 */
public class LoadingStrategy implements ILoadingStrategy {

    public void setLoadingFacility(ILoadingFacility facility) {
        mFacility = facility;
    }

    /**
     * This method doesn't follow the general concept, because instead of passing requests to
     * some Loading system for execution on local storage, it acts as this system itself. At first,
     * it was done because db support a lot of operatioin (add, remove, queries), so it seemed as a
     * good idea
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
            GeolookupData coord = new GeolookupData(fReq.getLat(), fReq.getLon());

            /** The case when we need only get data from Internet. Break the chain of command
             * and handle that request on network */
            if (fReq.getOnlineNoCache()) {
                reqState.isHandledLocally = true;
                mFacility.doRequestOnNetwork(reqState.requestID);
                //mFacility.updateRequestState(reqState);
                return;
            }
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
        /* perform network update if necessary */
        if (!state.isDone && isDataUpdateRequired(state)) {
            mFacility.doRequestOnNetwork(state.requestID);
        }
    }

    /**
     * Queues result processing immediately after current task. Result processing is much faster
     * than db access. By the way, at that time all db operations will be completed
     * @param state
     */
    @Override
    public void onNetworkResponse(final StateOfExecution state) {
        mFacility.getCallbackThreadHandler().postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                processNetworkResponse(state);
            }
        });
    }
    /**
     * update local data - delete old forecast for a given coordinates and store a new one, then
     * run callback on the calliong thread. This method is executed on a thread, responsible
     * for local storage and control operation (ILoadingFacility)
     * The checking for nullable reference is a mandatory, because when device suddenly goes
     * offline, all pending network tasks will result null, so we discard that request and
     * remove it from the registry. Thus no callbacks is called.
     * @param state
     */
    private void processNetworkResponse(final StateOfExecution state) {
        if (state == null || state.networkResponse == null) {
            state.isDone = true;
            mFacility.updateRequestState(state);
            return;
        }
        ILocalStorageRequests localStorage = mFacility.getLocalStorage();
        ForecastRequest fReq = (ForecastRequest) state.request;
        GeolookupData coord = new GeolookupData(fReq.getLat(), fReq.getLon());
        /** We don't save a new data if it is online request without cache */


        Forecast netResult = null;
        try {
            netResult =
                    (Forecast) ((ForecastResponse) state.networkResponse).getForecastData();
        }catch (ClassCastException e) {
            return;
        }catch (NullPointerException npe){
            return;
        }
        if (netResult == null) return;

        boolean isAdded = false;
        if (!fReq.getOnlineNoCache()) {
            localStorage.deletePlaceForecast(coord);
            isAdded = localStorage.addNewForecast(coord, netResult);
        }
        /* Notify calling (UI) */
        Handler callerThreaH = mFacility.getCallbackThreadHandler();
        callerThreaH.post(new Runnable() {
            @Override
            public void run() {
                state.callback.onResult(state.networkResponse);
            }
        });
        /** Remove current task from pending tasks list  */
        state.isDone = true;
        mFacility.updateRequestState(state);
    }

    /**
     * Verification algorythm, all obsolete data are supposed to be delted at this point
     * Here it checks the number of records in forecast. if it less than 3 (the rest have had been
     * deleted, then return true to initiate network update, otherwise return false
     * @param state
     * @return
     */
    private boolean isDataUpdateRequired(StateOfExecution state) {
        if (!state.isHandledLocally) {
            return false;
        }

        ForecastResponse r = (ForecastResponse) state.localResponse;
        if (r == null)
            return true;
        Forecast f = null;
        try {
            f = (Forecast) r.getForecastData();
        } catch (ClassCastException e) {
            return false;
        }
        /* At least three days in advance */
        if (f == null || f.mDayForecasts.size() < 3){
            return true;
        }
        if (mFacility.doNetworkUpdateWhenPossiible()) {
            return true;
        }
        return false;
    }
    private ILoadingFacility mFacility;
}
