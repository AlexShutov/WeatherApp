package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

/**
 * Created by Alex on 16.09.2015.
 */

import android.os.Handler;

import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.LocalStorage.ILocalStorageRequests;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;

/**
 * The purpose of this interface is to untie execution of every request processing stage
 * from decision about execution path. ILoadingStrategy is a behaviour- how to execute a
 * particular request depending on user preferences and system state. CachedLoadingSystem
 * can do local and network lookup, but local interface is wider than network requests, so this
 * interface partly incorporates local storage interface.
 */


public interface ILoadingFacility {

    void registerLoadingStrategy(ILoadingStrategy strategy);
    void initialRequest(RequestAbstract request, ICallback callback);
    void updateRequestState(StateOfExecution state);

    /**
     * Current state is passed into another process for execution; when it's done, result is return
     * back to control process and gets saved for further validation
     * @param stateID
     */
    void doRequestOnNetwork(Integer stateID);

    /**
     * Run this method on control thread when network operation is complete
     * @param state
     */
    void onNetworkResult(StateOfExecution state);

    /**
     * It is used only by cached strategy, only network or offline strategies doesn't need
     * this method
     * @return
     */
    boolean doNetworkUpdateWhenPossiible();

    /**
     * Callback must be executed on a calling thread, Handler of that thread is
     * stored in CachedLoadingSystem (UI thread). Strategy doesn't need to now that
     * @param result
     */
   // void passResult(StateOfExecution result);

    /**
     * The calls to local storage and result aanlysys is done on the same thread,
     * because db is fast, network requests bootlenecks execution. Db interface also is having mo
     * methods, so it is more easy to just pass it
     * @return used local storage reference
     */
    ILocalStorageRequests getLocalStorage();

    public Handler getCallbackThreadHandler();

    void processLocationRequest(StateOfExecution state);

}
