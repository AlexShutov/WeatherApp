package com.alex.weatherapp.MapsFramework.MapVisuals.Markers;

import android.location.Location;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Reaction;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Containers.IEntityContainer;
import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Alex on 15.11.2015.
 */

/**
 * I want to show marker in the place, tapped by user, that's why user would select new place
 * for adding. Then user may want to drag that marker, so drag events must not be interrupted.
 * But, this marker must be single- when user taps different place on the map, we need to remove
 * old marker and place new one. Summary: this reaction reacts to tap events, takes first data,
 * assuming it being PlaceData, then compares old position and new one. If it is within minimal
 * range, do nothing, but if not, update marker data with newer position, mark as changed and
 * force framework to reproject changed data. Add marker into data container with needed
 * parameters (color, type, etc.). If data container is empty, it would do nothing.
 */

public class SingleMarkerReaction extends Reaction {
    public static final String FEEDBACK_SINGLE_MARKER_CLICK = "feedback.single_marker_click";
    public SingleMarkerReaction(){
        super(ActionTypes.getActionType(ActionTypes.ACTION_MAP_TAP));
    }

    @Override
    protected boolean react(Action action) {
        if (action.isTunneled()){
            return false;
        }
        IEntityContainer projectionContainer = (IEntityContainer) getTargetEntity();
        IEntityContainer dataContainer = (IEntityContainer) projectionContainer.getEntangled();
        SocketRack sr = dataContainer.getCommunity().getSocketRack();
        if (dataContainer.getEntities().isEmpty()){
            Logger.w("SingleMarkerReaction: data container is having no marker data");
            return false;
        }
        /** there should be just one marker */
        PlaceData placeData = (PlaceData) dataContainer.getEntities().get(0);
        LocationData oldLocation = placeData.getLocation();
        LatLng tapPlace = (LatLng) action.getActionType().getExtra();

        oldLocation.setLat(tapPlace.latitude);
        oldLocation.setLon(tapPlace.longitude);

        placeData.setRequiresIndividualUpdate(true);
        sr.broadcastUpdate();

        Action feedback = new Action(ActionTypes.getActionType(FEEDBACK_SINGLE_MARKER_CLICK));
        LatLng fp = new LatLng(tapPlace.latitude, tapPlace.longitude);
        feedback.getActionType().setExtra(fp);
        sr.reactTo(feedback);
        return false;
    }

    @Override
    public boolean isSupportsAction(Action action) {
        return action.getActionType().getAction().equals(ActionTypes.ACTION_MAP_TAP);
    }
}
