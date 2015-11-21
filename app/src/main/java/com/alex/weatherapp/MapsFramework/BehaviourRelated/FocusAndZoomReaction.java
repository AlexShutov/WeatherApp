package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.alex.weatherapp.MapsFramework.Containers.Community;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.MapsFramework.Containers.ProjectionsWarehouse;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 15.11.2015.
 */
/** Reacts to user action. Allows to move and focus map on a given point. It is intended to be
 * family-level reaction. Use factory method to create instance.
 */
public class FocusAndZoomReaction extends Reaction {
    public static final String ACTION = "action.focus_and_zoom";
    public static class FocusAndZoomData {
        public FocusAndZoomData(){
            zoomLevel = 13;
            centerOfScreen = new LatLng(37.4218, -122.0840);
        }
        public int zoomLevel;
        public LatLng centerOfScreen;
    }
    public static IReaction newIsntance(){
        FocusAndZoomReaction r = new FocusAndZoomReaction();
        return ReactionOnMainThreadDecorator.decorate(r);
    }


    private FocusAndZoomReaction(){
        super(ActionTypes.getActionType(ACTION));
    }

    @Override
    protected boolean react(Action action) {
        FocusAndZoomData d = (FocusAndZoomData) action.getActionType().getExtra();

        ProjectionsWarehouse wh = (ProjectionsWarehouse) ((IEntityContainer)getTargetEntity()).getCommunity();
        GoogleMap map = wh.getMap();

        CameraUpdate newCamera = CameraUpdateFactory
                .newLatLngZoom(d.centerOfScreen, d.zoomLevel);
        map.moveCamera(newCamera);
        return true;
    }

    @Override
    public boolean isSupportsAction(Action action) {
        return action.getActionType().getAction().equals(ACTION);
    }
}
