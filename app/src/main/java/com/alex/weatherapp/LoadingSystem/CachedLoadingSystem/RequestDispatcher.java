package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.IRequestExecutor;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.LoadingSystem;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;
import com.alex.weatherapp.LoadingSystem.RequestExecutorTypeMismatchExceptioin;

/**
 * Created by Alex on 16.09.2015.
 */

 /**
 * Interceptor redirects necessary requests for handling in LocalLoadingSystem,
 * dispatcher hands them out to network system, so all other request will be executed
 * on network
 */

public class RequestDispatcher implements IRequestExecutor {

    public RequestDispatcher(CachedLoadingSystem ls) {
        mSmartLoadingSystem = ls;
    }

    /**
     * Sync wersion might be used by even higher level ececutor, but now it's useless,
     * because CachedLoadingSystem is supposed to handle async requests, redirection will
     * be done async too. This method is added just for symmetry
     * @param request
     * @return
     * @throws RequestExecutorTypeMismatchExceptioin
     * @throws IllegalStateException
     */
    @Override
    public IResponse execute(RequestAbstract request) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        LoadingSystem netwrkLdr = mSmartLoadingSystem.getNetworkLoadingSystem();
        IResponse result = null;
        result = netwrkLdr.execute(request);
        return result;
    }

    /**
     * Hands everything came here to network loader
     * @param request
     * @param callback
     * @throws RequestExecutorTypeMismatchExceptioin
     * @throws IllegalStateException
     */
    @Override
    public void execute(RequestAbstract request, ICallback callback) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        LoadingSystem netwrkLdr = mSmartLoadingSystem.getNetworkLoadingSystem();
        netwrkLdr.execute(request, callback);
    }

    /**
     * keep reference for request dispatching
     */
    CachedLoadingSystem mSmartLoadingSystem;
}
