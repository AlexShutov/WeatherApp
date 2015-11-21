package com.alex.weatherapp.MapsFramework.MapVisuals.Shapes;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTunnel;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MapTapAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Actions.MarkerDragAction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Reaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 13.11.2015.
 */
public class DragToTapConverter implements ActionTunnel.IActionConverter {
    @Override
    public Action convert(Action action) {
        MarkerDragAction src = (MarkerDragAction) action;
        LatLng point = src.getMarker().getPosition();
        MapTapAction ta = new MapTapAction();
        ta.setTapPosition(point);
        return ta;
    }

    @Override
    public ActionType getConvertedActionType() {
        return ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP);
    }
}
