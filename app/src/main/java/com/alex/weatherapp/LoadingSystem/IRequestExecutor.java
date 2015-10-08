package com.alex.weatherapp.LoadingSystem;

/**
 * Created by Alex on 05.09.2015.
 */

/**
 * Async and sync execution methods are almost the same, that redundancy is just because we don't need
 * a async call at all. Retrofit's callback is always executed on UI thread, but I think that it is better
 * to spawn its own control thread and from it execute network requests, because retrieved data has to be
 * be processed and this might take some time. Network request and local storage requests should be
 * started simultaneously, but local storage works faster, so we need to apply some decision strategy,
 * whether to show cached data or wait for a network request, and after that save newly downloaded data
 * Async methods is barely a test stub
 */
public interface IRequestExecutor {
    /* IllegalStateException is thrown when LoadingSystem has no Registered executers or when
     * none of them were being able to handle this request (thrown after the loop) */
    void execute(RequestAbstract request, ICallback callback) throws  RequestExecutorTypeMismatchExceptioin,
            IllegalStateException;

    IResponse execute(RequestAbstract request) throws RequestExecutorTypeMismatchExceptioin,
            IllegalStateException;

}
