package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

/**
 * Created by Alex on 16.09.2015.
 */


/**
 * Defines interface for processing requests. System consist of two data sources - local cache and
 * Internet. Request first come to the strategy, to be to be processed accordingly.
 * Expected behaviout: Assign id to request, save its callback and id aside execution data,
 * then queue this request for execution with a local storage. When this is done (asynchronously),
 * we need to find saved request state, verify data from local storage(db). For verification we
 * just check, whether data is actual (date for forecast). Then we need to clean up database from
 * obsolete data, so we queue task for these coordinate, whick will erase all obsolete forecasts.
 * After that stage we whether have cached data or not. If we are, inform caller about cached data
 * by caling callback with cached flag. If strategy assumes network lookup (depending on
 * wifi - 3g preferences and availibility), we queue network request. All requests must be done on
 * separate threads. After some time network request arrives and we inform caller about it.
 * Then we need to refresh out cached data with new forecasts. For that, erase all data for a
 * given coordinates and write anew.
 */

public interface ILoadingStrategy {
    void registerLoadingFacility(ILoadingFacility facility);
    /*
    Compared to ILoadingFacility's initialRequest, this one accepts StateOfExecution object, becase
    request is already registered
     */
    void initialRequest(StateOfExecution requestState);
    /**
     * @param state
     */
    void onLocalStorageResponse(StateOfExecution state);
    void onNetworkResponse(StateOfExecution state);
}
