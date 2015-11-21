package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Alex on 06.11.2015.
 */
public abstract class MapEventActionWrapperBase extends Reaction {
    public MapEventActionWrapperBase(GoogleMap map){
        super(null);
        mMap = map;
    }
    public GoogleMap getMap(){ return mMap;}

    public abstract void registerInGMap();
    public abstract void unregisterInGMap();

    public void setSocketRack(SocketRack rack){
        mSocketRack = rack;
    }

    @Override
    protected boolean react(Action action) {
        mSocketRack.react(action);
        return true;
    }

    @Override
    public boolean isSupportsAction(Action action) {
        return true;
    }

    protected GoogleMap mMap;
    protected SocketRack mSocketRack;

}
