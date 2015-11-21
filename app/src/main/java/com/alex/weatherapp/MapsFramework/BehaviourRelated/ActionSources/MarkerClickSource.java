package com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources;


import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerClickAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.MapEventActionWrapperBase;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Alex on 06.11.2015.
 */
public class MarkerClickSource extends MapEventActionWrapperBase
implements GoogleMap.OnMarkerClickListener{
    public MarkerClickSource(GoogleMap map){
        super(map);
    }

    @Override
    public void unregisterInGMap() {
        mMap.setOnMarkerClickListener(null);
    }


    @Override
    public void registerInGMap() {
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        MarkerClickAction action = new MarkerClickAction(marker);
        Logger.i(marker.getTitle() + "clicked");
        react(action);
        return true;
    }
}
