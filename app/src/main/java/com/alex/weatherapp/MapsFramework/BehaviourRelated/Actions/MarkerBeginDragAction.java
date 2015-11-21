package com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Alex on 06.11.2015.
 */
public class MarkerBeginDragAction extends Action {
    public MarkerBeginDragAction(Marker marker){
        super(ActionTypes.getActionType(ActionTypes.ACTION_DRAG_BEGIN));
        mMarker = marker;
    }
    public Marker getMarker(){ return mMarker;}

    private Marker mMarker;
}
