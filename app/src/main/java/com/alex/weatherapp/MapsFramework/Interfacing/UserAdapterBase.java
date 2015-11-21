package com.alex.weatherapp.MapsFramework.Interfacing;

import android.os.Handler;
import android.os.Looper;

import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionPlug;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.ActionType;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.SocketRack;
import com.alex.weatherapp.MapsFramework.Deployment.Deployer;
import com.alex.weatherapp.MapsFramework.MapFacade;
import com.alex.weatherapp.Utils.Logger;

/**
 * Created by Alex on 13.11.2015.
 */

/**
 * Subclass this class for providing connection of map system with the rest of application.
 * This class acts as event receiver (ActionPlug), and references SocketRack (event transmitter).
 *  Plug this adapter into SocketRack for necessary event types and handle incoming events in
 *  handleAction(Action) as response to interaction with map, calling methods from IFeedback
 *  derived interface (User->Map events -> SocketRack event-> Reaction execution + result broadcast
 *  -> SocketRack -> UserAdapterBase plug receipt -> IFeedback call ). It works the same way in
 *  opposite direction: (App changes data -> ISysIFace call (BaseAdapter derived) ->
 *  -> socketRack user event broadcast -> IReaction fires and executes -> Map projection
 *  -> Map view change )
 *  Return and accept a covariant feedback iface in subclass
 *  User adapter may receive actions from worker thread, but UI can be touched only from
 *  main thread, that's why custom IActionHandler is used
 */

public abstract class UserAdapterBase extends ActionPlug
        implements ActionPlug.IActionHandler, ISysInterface {
    private class ActionMarshallerDecorator implements IActionHandler {
        ActionMarshallerDecorator(){
            mMainThrdHandler = new Handler(Looper.getMainLooper());
        }
        @Override
        public void handleAction(final Action action) {
            mMainThrdHandler.post(new Runnable() {
                @Override
                public void run() {
                    UserAdapterBase.this.handleAction(action);
                }
            });
        }
        private Handler mMainThrdHandler;
    }

    public UserAdapterBase(){
        super(null);
        mFacade = null;
        setActionHandler(new ActionMarshallerDecorator());
    }

    /** We may want to stop working with this adapter for some reason, perhaps, we
     * changed out mind and want to use a different behaviour, so we need to get rid
     * of old behaviour first, and then setup a new one.
     */
    public void activate(Deployer deployer){
        setMapFacade(deployer.getFacade());
        activateAdapter(deployer);
    }
    public abstract void activateAdapter(Deployer deployer);


    public SocketRack getSocketRack()throws IllegalStateException{
        return mFacade.getSocketRack();
    }
    public MapFacade getFacade(){ return mFacade;}
    public void setMapFacade(MapFacade facade){
        mFacade = facade;
    }

    protected void addFeedbackAction(ActionType actionType){
        this.plugIntoRack(getSocketRack(), actionType);

    }
    protected void removeAction(ActionType actionType){
        if (isSupportsAction(actionType)){
            Logger.e("Trying to unplug user adapter from action never being added");
            return;
        }
        this.unplugFromRack(getSocketRack(), actionType);
    }


    private MapFacade mFacade;
}
