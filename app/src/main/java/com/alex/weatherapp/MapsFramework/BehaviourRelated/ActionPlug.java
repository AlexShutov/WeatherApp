package com.alex.weatherapp.MapsFramework.BehaviourRelated;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionSources.UserActionsRelay;
import com.alex.weatherapp.Utils.Logger;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Alex on 06.11.2015.
 */
public class ActionPlug implements Comparable {
    public interface IActionHandler{
        void handleAction(Action action);
    }
    private static class PlugIDGenerator{
        static {
            sPlugNo = 0;
        }
        public static int getNextPlugID(){
            return sPlugNo++;
        }
        private static int sPlugNo;
    }

    public ActionPlug(IActionHandler actionHandler){
        mActionHandler = actionHandler;
        mSocketPlugedIn = null;
        mPlugID = PlugIDGenerator.getNextPlugID();
        mPluggedActions = new LinkedHashSet<>();
    }
    public boolean isPlugged(){ return mSocketPlugedIn == null;}
    public int getPlugID(){ return mPlugID;}

    public void acceptAction(Action action){
        mActionHandler.handleAction(action);
    }

    /** This plug might already be plugged to many sockets, so to avoid re-plugging due to
     * creation of a new ActionPlug, we may just change the underlying action handler.
     * @param handler
     */
    public void setActionHandler(IActionHandler handler){
        mActionHandler = handler;
    }

    /** Potentially plug may be plugged to any socket, use this method to plug
     * in single socket
     * @param socket
     */
    public void plug(ActionSocket socket){
        socket.acceptPlugging(this);
        mSocketPlugedIn = socket;
    }
    public void unplug(){
        if (!isPlugged()) return;
        mSocketPlugedIn.unplug(this);
        mSocketPlugedIn = null;
    }

    public void plugIntoRack(SocketRack rack, ActionType actionType){
        if (mPluggedActions.contains(actionType)){
            Logger.w("Trying to plug for the same action again: " + actionType);
            return;
        }
        IActionSocket socket = rack.getSocket(actionType);
        if (null == socket){
            Logger.i("Trying to plug into not existing socket: "+ actionType.getAction()+
                    ", assuming user Action");
            UserActionsRelay relay = new UserActionsRelay();
            rack.addActionSourceAndSocket(relay, relay, actionType);
            socket = rack.getSocket(actionType);
        }
        socket.acceptPlugging(this);
        mPluggedActions.add(actionType);
    }

    public void unplugFromRack(SocketRack rack, ActionType actionType){
        if (!mPluggedActions.contains(actionType)){
            Logger.w("Trying to unplug action, this plug weren't being plugged for: " + actionType);
            return;
        }
        IActionSocket socket = rack.getSocket(actionType);
        if (null == socket){
            return;
        }
        socket.unplug(this);
        mPluggedActions.remove(actionType);
    }
    public void unplugAll(SocketRack rack){
        /** for not breaking iterator */
        Set<ActionType> temp = new LinkedHashSet<>(mPluggedActions);
        for (ActionType type : temp){
            unplugFromRack(rack, type);
        }
    }
    public Set<ActionType> getActionsOfPluggedSockets(){
        return new HashSet<>(mPluggedActions);
    }
    public boolean isSupportsAction(ActionType actionType){
        boolean is = mPluggedActions.contains(actionType);
        return is;
    }

    protected Set<ActionType> getPluggedActions(){ return mPluggedActions;}

    @Override
    public int compareTo(Object another) {
        ActionPlug p2 = (ActionPlug)another;
        return new Integer(mPlugID).compareTo(p2.mPlugID);
    }

    private Set<ActionType> mPluggedActions;
    private ActionSocket mSocketPlugedIn;
    private IActionHandler mActionHandler;
    private int mPlugID;
}
