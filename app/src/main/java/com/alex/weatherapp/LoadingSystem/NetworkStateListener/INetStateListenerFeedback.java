package com.alex.weatherapp.LoadingSystem.NetworkStateListener;

/**
 * Created by Alex on 24.09.2015.
 */

/**
 * These methods gets called by listener when connection state changes
 */
public interface INetStateListenerFeedback {
    void onOffline();
    void onOnline();

    /** TODO: Use bandwith and traffic control in the future
     * These last two might be use to choose whether to download
     * images or not
     */
    void onWiFiAvailible();
    void onCellularAvailible();
}

