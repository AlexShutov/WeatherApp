package com.alex.weatherapp.MapsFramework.BehaviourRelated.Projections;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionPlug;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionTypes;

import java.util.Set;

/**
 * Created by Alex on 08.11.2015.
 */

/**
 * Mediator between projector and event receipt. Parses basic projection
 * events and calls according Projector's methods;
 */

public class ProjectorActionPlug implements ActionPlug.IActionHandler {
    public ProjectorActionPlug(IProjector projector){
        mProjector = projector;
    }

    @Override
    public void handleAction(Action action) {
        String actionType = action.getActionType().getAction();
        Set<String> projectionEvents = ActionTypes.getProjectionActionsString();
        if (!projectionEvents.contains(actionType)){
            return;
        }
        switch (action.getActionType().getAction()){
            case ActionTypes.ACTION_CLEAR_PROJECTION:
                Object extra = action.getActionType().getExtra();
                // pass something as extra in action if you want to wipe all map, not just
                // projections from a given family
                if (null == extra) {
                    mProjector.clearProjections(false);
                } else {
                    mProjector.clearProjections(true);
                }
                break;
            case ActionTypes.ACTION_USER_UPDATE_REQUESTED_ITEMS:
                mProjector.updateRequestedItems();
                break;
            case ActionTypes.ACTION_USER_MAKE_INITIAL_PROJECTION:
                mProjector.createEmptyProjections();
                break;
            case ActionTypes.ACTION_USER_PROJECT:
                mProjector.project(mProjector.getDataContainer(), mProjector.getProjectionContainer());
                break;
            case ActionTypes.ACTION_REPROJECT:
                mProjector.clearProjections(false);
                mProjector.createEmptyProjections();
                mProjector.project(mProjector.getDataContainer(), mProjector.getProjectionContainer());
                break;
            default:
                break;
        }
    }

    /** Immutable */
    public IProjector getProjector(){
        return mProjector;
    }

    private IProjector mProjector;
}
