package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources.MapTapSource;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources.MarkerClickSource;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources.MarkerDragEventSource;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources.UserActionsRelay;
import com.google.android.gms.maps.GoogleMap;

import java.util.Set;


/**
 * Created by Alex on 06.11.2015.
 */
public class StandartSocketRackBuilder {
    public StandartSocketRackBuilder(){
        mMap = null;
        mRackToBuild = null;
    }
    public void createNewRack(GoogleMap map){
        mRackToBuild = new SocketRack();
        mMap = map;
    }

    public void enableMarkerClicks(){
        if (null == mMap){
            return;
        }
        MarkerClickSource actionSrc = new MarkerClickSource(mMap);
        mRackToBuild.addActionSource(actionSrc, ActionTypes.getActionType(ActionTypes.ACTION_MARKER_CLICK));
    }
    public void enableMarkerDragging(){
        if (null == mMap){
            return;
        }
        MarkerDragEventSource actionSrc = new MarkerDragEventSource(mMap);
        mRackToBuild.addActionSource(actionSrc, ActionTypes.getActionType(ActionTypes.ACTION_DRAG_BEGIN));
        mRackToBuild.addActionSource(actionSrc, ActionTypes.getActionType(ActionTypes.ACTION_DRAG));
        mRackToBuild.addActionSource(actionSrc, ActionTypes.getActionType(ActionTypes.ACTION_DRAG_END));
    }
    public void enableMapTapping(){
        MapTapSource actionSrc = new MapTapSource(mMap);
        mRackToBuild.addActionSource(actionSrc, ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP));
    }

    protected void enableProjectionActions(){
        Set<ActionType> res = ActionTypes.getProjectionActions();
        for (ActionType a: res){
            addUserActionRelay(a);
        }

    }
    protected void addUserActionRelay(ActionType actionType){
        UserActionsRelay relay = new UserActionsRelay();
        mRackToBuild.addActionSourceAndSocket(relay, relay, actionType);
    }

    public SocketRack build(){
        enableProjectionActions();
        SocketRack sr = mRackToBuild;
        mRackToBuild = null;
        mMap = null;
        return sr;
    }

    private SocketRack mRackToBuild;
    private GoogleMap mMap;
}
