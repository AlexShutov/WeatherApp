package com.alex.weatherapp.LoadingSystem.NetworkStateListener;

/**
 * Created by Alex on 24.09.2015.
 */
public interface INetStateListenerControl {
    void startListening();
    void stopListening();

    void setFeedback(INetStateListenerFeedback feedback);
    void forceStateChecking();
}
