package com.alex.weatherapp.LoadingSystem.ServiceWrapper;

import com.alex.weatherapp.LoadingSystem.ILoadingFacade;

/**
 * Created by Alex on 27.09.2015.
 */

/**
 * Serves the same function, as onBound, onUnbound. But, on the other hand, wrapped
 * service is supposed to linger for some time in memory after all recipients is unbound, because there
 * may be only one screen bound to  it at a time. When service is unbound due to
 * screen rotation, there may still be pending task. I may solve that by starting
 * the service with start() and REDELIVERY_INTENT and loiter timer, preventing service
 * from dying for extra few seconds.
 */

public interface ILoadingConnection {
    interface IConnectedCallback {
        void onConnected(ILoadingFacade mConnectedSystem);
        void onDisconnected();
    }

    /**
     * @return It is retrives false if implementing instance is already
     * conected to the service
     */
    boolean connect();
    boolean disconnect();
    void setOnConnectedCallback(IConnectedCallback callback);
    boolean isConnected();
}


