package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastRequest;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationRequest;
import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.IRequestExecutor;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;
import com.alex.weatherapp.LoadingSystem.RequestExecutorTypeMismatchExceptioin;

/**
 * Created by Alex on 16.09.2015.
 */
public class RequestInterceptor implements IRequestExecutor {

    public RequestInterceptor(CachedLoadingSystem.ExecutionEntryPoint entryPoint) {
        mEntryPoint = entryPoint;
    }

    @Override
    public IResponse execute(RequestAbstract request) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        if (validate(request)) {
            return mEntryPoint.execute(request);
        } else {
            /**
             * unsupported kind pass request down the chain of executors
             */
            throw new RequestExecutorTypeMismatchExceptioin();
        }
    }

    @Override
    public void execute(RequestAbstract request, ICallback callback) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        if (validate(request)) {
            mEntryPoint.execute(request, callback);
        } else {
            /**
             * unsupported kind pass request down the chain of executors
             */
            throw new RequestExecutorTypeMismatchExceptioin();
        }
    }

    /**
     * Accept only forecast and geolookup requests
     * @param request
     * @return
     */
    private boolean validate(RequestAbstract request) {
        if (request instanceof ForecastRequest ) return true;
        if (request instanceof LocationRequest) return true;
        return false;
    }

    private CachedLoadingSystem.ExecutionEntryPoint mEntryPoint;
}
