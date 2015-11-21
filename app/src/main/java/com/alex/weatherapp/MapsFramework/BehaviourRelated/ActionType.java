package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 06.11.2015.
 */



/**
 * Type of the action send by the map, add new if you want to expand app's functionality,
 * Action might be generated not onluy by a map, but also by programm in response of some data
 * change. This enum allows avoiding RTTI checking during action dispatching and also used for
 * distinguishing ActionSockets
 */
public class ActionType implements Comparable {
    public ActionType(){
        mAction = "";
        mExtra = null;
    }

    public void setAction(String action){
        mAction = action;
    }
    public String getAction(){ return mAction;}
    public void setExtra(Object extra){
        mExtra = extra;
    }
    public Object getExtra(){
        return mExtra;
    }

    @Override
    public boolean equals(Object o) {
        ActionType a2 = (ActionType)o;
        return mAction.equals(a2.mAction);
    }

    @Override
    public int hashCode() {
        return mAction.hashCode();
    }

    @Override
    public int compareTo(Object another) {
        ActionType a2 = (ActionType)another;
        return mAction.compareTo(a2.mAction);
    }

    private String mAction;
    private Object mExtra;
}
