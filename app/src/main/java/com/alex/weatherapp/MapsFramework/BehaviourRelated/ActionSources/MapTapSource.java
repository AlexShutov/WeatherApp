package com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.MapEventActionWrapperBase;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 10.11.2015.
 */
public class MapTapSource extends MapEventActionWrapperBase
        implements GoogleMap.OnMapClickListener{
    public MapTapSource(GoogleMap map){ super(map);}

    @Override
    public void onMapClick(LatLng latLng) {
        MapTapAction action = new MapTapAction();
        action.setTapPosition(latLng);
        Logger.i("place tapped on map: " + latLng.toString());
        react(action);
    }

    @Override
    public void registerInGMap() {
        getMap().setOnMapClickListener(this);
    }

    @Override
    public void unregisterInGMap() {
        getMap().setOnMapClickListener(this);
    }


}
