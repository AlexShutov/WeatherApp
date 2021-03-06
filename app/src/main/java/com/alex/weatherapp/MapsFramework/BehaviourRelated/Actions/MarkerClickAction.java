package com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Alex on 06.11.2015.
 */
public class MarkerClickAction extends Action {
    public MarkerClickAction(Marker marker) {
        super(ActionTypes.getActionType(ActionTypes.ACTION_MARKER_CLICK));
        getActionType().setExtra(marker);
    }
    public Marker getClickedMarker(){
        return (Marker) getActionType().getExtra();
    }
}
