package com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 10.11.2015.
 */

public class MapTapAction extends Action {
    public MapTapAction(){
        super(ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP));
    }
    public LatLng getTapPosition(){
        return (LatLng) getActionType().getExtra();
    }
    public void setTapPosition(LatLng geoPosition){
        this.getActionType().setExtra(geoPosition);
    }
}
