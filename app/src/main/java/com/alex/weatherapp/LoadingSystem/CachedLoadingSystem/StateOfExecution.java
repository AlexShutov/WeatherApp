package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;

/**
 * Created by Alex on 17.09.2015.
 */

 /*
    Right after request is passed to this loader, it gets unique id and information about it
    (request arg and callback) are stored in the map for further handling. This class just 'structure'
    without any methods, so it has no accessors
    */
public class StateOfExecution {
    /* Only few request types are cached, here it is used to avoid
    type checking and grow fo subclasses
     */
    public StateOfExecution() {
        requestType = RequestType.ForecastRequest;
        requestID = 0;
        isHandledLocally = false;
        isHandledOnNetwork = false;
        request = null;
        callback = null;
        isDone = false;
    }

    enum RequestType {
        Geolookup,
        ForecastRequest,
        LocationRequest
    }
    RequestType requestType;
    public Integer requestID;
    public boolean isHandledLocally;
    IResponse localResponse;
    public boolean isHandledOnNetwork;
    public IResponse networkResponse;
    public boolean isDone;

    public RequestAbstract request;
    public ICallback callback;
}
