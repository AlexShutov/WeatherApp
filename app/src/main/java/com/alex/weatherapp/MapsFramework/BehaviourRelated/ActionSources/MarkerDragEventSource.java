package com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerBeginDragAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerDragAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerEndDragAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.MapEventActionWrapperBase;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Alex on 06.11.2015.
 */
public class MarkerDragEventSource extends MapEventActionWrapperBase
        implements GoogleMap.OnMarkerDragListener{
    public MarkerDragEventSource(GoogleMap map){
        super(map);
    }

    @Override
    public void unregisterInGMap() {
        mMap.setOnMarkerClickListener(null);
    }


    @Override
    public void registerInGMap() {
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        MarkerBeginDragAction action = new MarkerBeginDragAction(marker);
        react(action);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        MarkerDragAction action = new MarkerDragAction(marker);
        react(action);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        MarkerEndDragAction action = new MarkerEndDragAction(marker);
        react(action);
    }

}
