package com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionPlug;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSocket;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.IActionSocket;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.MapEventActionWrapperBase;
import com.alex.weatherapp.Utils.Logger;

/**
 * Created by Alex on 08.11.2015.
 */

/**
 * The purpose of this class is in providing ability to communicate with each other to families of
 * entities. By default message pump broadcasts only device-level events to all subscribers:
 * touch, dragging, (add some new).  Suppose, we have a set of markers on the map and areas around
 * it. Areas should change its color when marker is clicked, or when external app layer demands.
 * Action passing between families solves this problem. The only drawback- when there are too many
 * families, callback stack may grow large. If it happens, try to use Handler's message queue.
 * This class is a source of events and receiver simultaneously. It has and is IActionSocket
 * instead of multiple inheritance.
 * I you want to add user action, create new instance of this class and put it into SocketRack
 * under corresponding ActionType tag. Every new user action needs separate relay, because
 * families connects to it.
 */

public class UserActionsRelay extends MapEventActionWrapperBase implements IActionSocket {
    /** Due to inheritance from MapEventActionWrapperBase it has null reference to a GMap. We may
     * get rod of it by extracting interface, but I didn't to avoid boilerplate code
     */
    public UserActionsRelay(){
        super(null);
        mSocket = new ActionSocket();
    }

    /** Inherited from MapEventActionWrapperBase */
    /**
     * Here those methods has nothing to do with a map, because they just retransmits
     * user actions
     */
    @Override
    public void registerInGMap() {
        Logger.i("Attempting to register relay in map");
    }
    @Override
    public void unregisterInGMap() {
        Logger.i("Attempting unregistering relay i map");
    }

    /** Inherited from IActionSocket */

    /**
     *
     * @param plug
     * @return
     */
    @Override
    public boolean isPlugged(ActionPlug plug) {
        return mSocket.isPlugged(plug);
    }

    @Override
    public void acceptPlugging(ActionPlug plug) {
        mSocket.acceptPlugging(plug);
    }

    @Override
    public boolean unplug(ActionPlug plug) {
        return mSocket.unplug(plug);
    }

    @Override
    public void unplugAll() {
        mSocket.unplugAll();
    }

    @Override
    public int getPluggedCnt() {
        return mSocket.getPluggedCnt();
    }

    @Override
    public void broadcastAction(Action action) {
        mSocket.broadcastAction(action);
    }

    @Override
    public void pause() {
        mSocket.pause();
    }

    @Override
    public int resume() {
        return mSocket.resume();
    }

    @Override
    public void setIgnoreDuringPause(boolean ignore) {
        mSocket.setIgnoreDuringPause(ignore);
    }

    @Override
    public boolean isActive() {
        return mSocket.isActive();
    }

    @Override
    public boolean isIgnoringDuringPause() {
        return mSocket.isIgnoringDuringPause();
    }

    private IActionSocket mSocket;
}
