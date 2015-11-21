package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.alex.weatherapp.Utils.Logger;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Alex on 06.11.2015.
 */
/** Maintains a set of Action plugs and dispatches every occured Action to them */
public class ActionSocket implements IActionSocket {
    private static final int MAX_QUEUE_SIZE = 1000;
    public ActionSocket(){
        init();
    }
    private void init(){
        mPluggedInPlugs = new LinkedList<>();
        mPluggedPlugsIDs = new TreeSet<>();
        mPendingActions = new ArrayDeque<>();

        mIsDispatching = true;
        mIgnoreDuringPause = false;
    }
    @Override
    public boolean isPlugged(ActionPlug plug){
        if (null == plug){
            return false;
        }
        return mPluggedPlugsIDs.contains(plug.getPlugID());
    }

    /**
     * Plug is plugged into socket, not otherwise
     * @param plug
     */
    @Override
    public void acceptPlugging(ActionPlug plug){
        if (!isPlugged(plug)){
            mPluggedInPlugs.add(plug);
            mPluggedPlugsIDs.add(plug.getPlugID());
        }
    }
    @Override
    public boolean unplug(ActionPlug plug){
        if (!isPlugged(plug)){
            return false;
        }
        pause();
        /** 0(N^2) */
        mPluggedInPlugs.remove(mPluggedInPlugs.indexOf(plug));
        mPluggedPlugsIDs.remove(plug.getPlugID());
        resume();
        return false;
    }
    @Override
    public void unplugAll(){
        init();
    }
    @Override
    public int getPluggedCnt(){
        return mPluggedPlugsIDs.size();
    }
    /** Notifies every plluged in plug about new action arrived, and if socket is paused and
     * action queuing is allowed, stack that action for further handling. In the case when number of
     * pending actions exceeds a given threshold, earlier actions gets erased and replaced by  a
     * new ones*/
    @Override
    public void broadcastAction(Action action){
        Logger.d("Action socket action received: "+ action.getActionType().getAction());
        if (!isActive()){
            if (isIgnoringDuringPause()){
                return;
            }
            if (mPendingActions.size() >= MAX_QUEUE_SIZE){
                mPendingActions.element();
                mPendingActions.offer(action);
            }
            mPendingActions.offer(action);
            return;
        }
        for (ActionPlug p : mPluggedInPlugs){
            if (p.isSupportsAction(action.getActionType())) {
                p.acceptAction(action);
            }
        }
    }
    @Override
    public void pause(){
        mIsDispatching = true;
    }
    /** Broadcasts all pending actions and turns off pending mode */
    @Override
    public int resume(){
        int nPending = broadcastPending();
        mIsDispatching = true;
        return nPending;
    }
    @Override
    public void setIgnoreDuringPause(boolean ignore){
        mIgnoreDuringPause = ignore;
    }
    @Override
    public boolean isActive(){ return mIsDispatching;}
    @Override
    public boolean isIgnoringDuringPause(){ return mIgnoreDuringPause;}

    /**
     * if mIsDispatching equals true, broadcasts all pending actions from a Queue;
     * @return The number of pending actions were being broadcast.
     */
    private int broadcastPending(){
        int nPending = mPendingActions.size();
        while (!mPendingActions.isEmpty()){
            broadcastAction(mPendingActions.poll());
        }
        return nPending;
    }

    public boolean mIsDispatching;
    public boolean mIgnoreDuringPause;
    private Queue<Action> mPendingActions;
    private Set<Integer> mPluggedPlugsIDs;
    private List<ActionPlug> mPluggedInPlugs;
}
