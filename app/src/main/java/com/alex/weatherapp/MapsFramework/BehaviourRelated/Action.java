package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 06.11.2015.
 */


/** Base class for all actions. By default carries action type.
 * Action has data, related to a particular type of action, so that field is
 * immutable*/
public class Action {
    public Action(ActionType actionType){
        mActionType = actionType;
        mIsTunneled = false;
        mUserOnMainThread = false;
    }
    public ActionType getActionType() { return mActionType;}
    public boolean isUserOnMainThread(){ return mUserOnMainThread;}
    public void setUserOnMainThread(boolean is){ mUserOnMainThread = is;}
    public boolean isTunneled(){ return  mIsTunneled;}
    public void setIsTunneled(boolean isTunneled){ mIsTunneled = isTunneled;}

    private ActionType mActionType;
    private boolean mUserOnMainThread;
    private boolean mIsTunneled;
}

