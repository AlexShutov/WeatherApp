package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 08.11.2015.
 */
public interface IActionSocket {

    boolean isPlugged(ActionPlug plug);
    void acceptPlugging(ActionPlug plug);
    boolean unplug(ActionPlug plug);
    void unplugAll();
    int getPluggedCnt();
    void broadcastAction(Action action);
    void pause();
    int resume();
    void setIgnoreDuringPause(boolean ignore);
    boolean isActive();
    boolean isIgnoringDuringPause();
}
