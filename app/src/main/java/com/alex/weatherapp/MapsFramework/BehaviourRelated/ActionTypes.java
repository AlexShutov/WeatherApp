package com.alex.weatherapp.MapsFramework.BehaviourRelated;


import android.support.v4.util.Pools;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alex on 08.11.2015.
 */
public class ActionTypes {
    /** Wrappers for user-map interaction events */
    public static final String ACTION_MARKER_CLICK = "action.marker_click";
    public static final String ACTION_DRAG_BEGIN = "action.drag_begin";
    public static final String ACTION_DRAG = "action.drag";
    public static final String ACTION_DRAG_END = "action.drag_end";
    public static final String ACTION_MAP_TAP = "action.map_tap";
    /** Algorithmic commands */
    public static final String ACTION_REPROJECT = "action.reproject";
    public static final String ACTION_UPDATE_DATA = "action.update_data";
    public static final String ACTION_NOTIFY_USER = "action.notify_user";
    /** Basic user commands */
    /** create projection entities without attaching data to map (for speed) */
    public static final String ACTION_USER_MAKE_INITIAL_PROJECTION = "action.make_initial_projection";
    /** perform projection (draw everything you need on a map */
    public static final String ACTION_USER_PROJECT = "action.project";
    /** forces projector to clear and reproject projections, which data is marked as requiring
     * update
     */
    public static final String ACTION_USER_UPDATE_REQUESTED_ITEMS = "action.reproject_requested";
    /** clear GMap in response along with all projections */
    public static final String ACTION_CLEAR_PROJECTION = "action.clear_projection";

    /** Factory method, creating a new action, but it is supposed to use a pool of actions */
    public static ActionType getActionType(String action, Object extra){
        ActionType actionType = new ActionType();
        actionType.setAction(action);
        actionType.setExtra(extra);
        if (sDefaultActions.contains(action)){
            if (null == extra){
                Long time = System.currentTimeMillis();
                actionType.setExtra(time);
            }
        }
        return actionType;
    }
    public static ActionType getActionType(String action){
        return getActionType(action, null);
    }
    public static Action getAction(String action, Object extra){
        return new Action(getActionType(action, extra));
    }
    public static Action getAction(String action){
        return getAction(action, null);
    }


    /** we don't want reference values being spoiled by user, so don't change these values
     * @return
     */
    public static Set<ActionType> getProjectionActions(){
        Set<ActionType> acts = new HashSet<>();
        for (String s : sProjectionActions){
            acts.add(getActionType(s));
        }
        return acts;
    }
    public static Set<String> getProjectionActionsString(){
        return sProjectionActions;
    }

    public static Set<ActionType> getDefaultActions(){
        Set<ActionType> acts = new HashSet<>();
        for (String s : sDefaultActions){
            acts.add(getActionType(s));
        }
        return acts;
    }

    private static Set<String> sDefaultActions;
    private static Set<String> sProjectionActions;
    static {
        sDefaultActions = new HashSet<>();
        sDefaultActions.add(ACTION_MARKER_CLICK);
        sDefaultActions.add(ACTION_DRAG_BEGIN);
        sDefaultActions.add(ACTION_DRAG);
        sDefaultActions.add(ACTION_DRAG_END);
        sDefaultActions.add(ACTION_MAP_TAP);

        sProjectionActions = new HashSet<>();
        /** Add projection Actions*/
        sProjectionActions.add(ACTION_USER_MAKE_INITIAL_PROJECTION);
        sProjectionActions.add(ACTION_USER_PROJECT);
        sProjectionActions.add(ACTION_USER_UPDATE_REQUESTED_ITEMS);
        sProjectionActions.add(ACTION_CLEAR_PROJECTION);
        sProjectionActions.add(ACTION_REPROJECT);
        sProjectionActions.add(ACTION_NOTIFY_USER);
        sProjectionActions.add(ACTION_UPDATE_DATA);
    }
}
