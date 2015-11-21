package com.alex.weatherapp.MapsFramework.BehaviourRelated;

/**
 * Created by Alex on 06.11.2015.
 */


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.alex.weatherapp.MapsFramework.EntityGeneral.IEntity;
import com.alex.weatherapp.Utils.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Serves as action pump. every entity knowing about it might broadcast action, which is
 * delivered to all subscribers regardless of data-projection nature of recipient.
 * Contains sockets for all supported actions, action plug takes socket from here, also has a
 * Google map event wrapper for every kind of action. Event wrapper forms action of its kind, then
 * hands it to this rack, which, in turn, dispatches it to appropriate socket
 */
public class SocketRack extends Reaction {
    class AsyncWorker extends HandlerThread implements IReaction {
        class WorkerHandler extends Handler{
            public WorkerHandler(Looper looper) {
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                /** make sure nothing gets out of here */
                try {
                    Action action = (Action) msg.obj;
                    IActionSocket socket = mSockets.get(action.getActionType());
                    if (null != socket) {
                        socket.broadcastAction(action);
                    } else {
                        Logger.e("Trying to send action to not existing socket: " +
                                action.getActionType().getAction());
                    }
                }catch (Exception e){
                    Logger.e("Exception on a worker thread: " + e.getMessage());
                }
            }
        }
        protected AsyncWorker(){
            super("MapsHelperMsgPumpThread");
            this.setPriority(Thread.MIN_PRIORITY);
            mHandler = null;
            mMainHandler = new Handler(Looper.getMainLooper());
        }

        /** Inherited from  IReaction */
        @Override
        public void setTargetEntity(IEntity target) {
        }
        @Override
        public IEntity getTargetEntity() {
            return null;
        }
        @Override
        public boolean isSupportsAction(Action action) {
            return true;
        }
        @Override
        public boolean reactTo(final Action action) {
            if (false){
                reactOnMainThread(action);
            }else {
                Message msg = mHandler.obtainMessage(0, action);
                mHandler.sendMessage(msg);
            }
            return true;
        }

        private void reactOnMainThread(final Action action){
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    IActionSocket socket = mSockets.get(action.getActionType());
                    if (null != socket) {
                        socket.broadcastAction(action);
                    }else {
                        Logger.e("Trying to send action to not existing socket: "+
                        action.getActionType().getAction());
                    }
                }
            });
        }

        @Override
        protected void onLooperPrepared() {
            mHandler = new WorkerHandler(Looper.myLooper());
        }
        private WorkerHandler mHandler;
        private Handler mMainHandler;
    }

    public SocketRack(){
        super(null);
        init();
    }

    /**
     * All actions related to an actual GoogleMap must be handled on the main thread.
     * Those include projection actions and standard actions (clicks, drags, taps, etc.)
     * We create and start worker thread for user actions here
     */
    private void init(){
        mSockets = new TreeMap<>();
        mActionSources = new TreeMap<>();
        mIsPaused = false;
        mWorkerThread = new AsyncWorker();
        mWorkerThread.start();
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mProjectorActions = new HashSet<>();
        mProjectorActions.addAll(ActionTypes.getProjectionActions());
       // mProjectorActions.addAll(ActionTypes.getDefaultActions());
    }

    public void activate(){
        for (Map.Entry<ActionType, MapEventActionWrapperBase> e : mActionSources.entrySet()){
            e.getValue().registerInGMap();
        }
    }
    public void deactivate(){
        for (Map.Entry<ActionType, MapEventActionWrapperBase> e : mActionSources.entrySet()){
            e.getValue().unregisterInGMap();
        }
    }
    /** postpone all coming events and save them for future handling (system isn't
    * initialized yet)
     */
    public void pause(){
        if (mIsPaused){
            return;
        }
        mIsPaused = true;
        for (Map.Entry<ActionType, IActionSocket> e: mSockets.entrySet()){
            IActionSocket socket = e.getValue();
            socket.pause();
        }
        mWorkerThread.getLooper().quit();
    }
    public void destroy(){
        mWorkerThread.getLooper().quit();
    }

    /**
     * Resume accepting actions of all types, but first handle actions in the queues
     * If worker thread is finished, create new thread and start it.
     */
    public void resume(){
        if (!mIsPaused) {
            return;
        }
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        if (!mWorkerThread.isAlive()){
            mWorkerThread = new AsyncWorker();
            mWorkerThread.start();
        }
        for (Map.Entry<ActionType, IActionSocket> e: mSockets.entrySet()){
            IActionSocket socket = e.getValue();
            socket.resume();
        }
        mIsPaused = false;
    }

    /** all operations with the map must be done on main thread, so we need to
     * distinguish projector actions from user actions and hand user actions to
     * worker thread.
     */
    @Override
    protected boolean react(Action action) {
       // IActionSocket socket = mSockets.get(action.getActionType());
      //  socket.broadcastAction(action);
        if (mProjectorActions.contains(action.getActionType()) ||
                action.isUserOnMainThread()){
            reactOnMainThread(action);
        }else {
            mWorkerThread.reactTo(action);
        }
        return true;
    }
    private void reactOnMainThread(final Action action) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                IActionSocket socket = mSockets.get(action.getActionType());
                socket.broadcastAction(action);
            }
        });
    }

    @Override
    public boolean isSupportsAction(Action action) {
        return true;
    }

    public IActionSocket getSocket(ActionType actionType){
        return mSockets.get(actionType);
    }
    public MapEventActionWrapperBase getActionSource(ActionType actionType){
        return mActionSources.get(actionType);
    }

    /**
     * Adds new action source, action socket for type of action 'actionType'
     * @param source source of actions
     * @param socket socket for that kind of actions
     * @param actionType type of actions
     * @throws IllegalArgumentException is thrown when that rack is already having socket for that
     * type of actions
     */
    public void addActionSourceAndSocket(MapEventActionWrapperBase source,
                            IActionSocket socket,
                            ActionType actionType) throws IllegalArgumentException{
        if (mSockets.containsKey(actionType)) {
             throw new IllegalArgumentException(
                     "Socket rack already supports this kind of action");
        }
        mSockets.put(actionType, socket);
        mActionSources.put(actionType, source);
        source.setSocketRack(this);
    }

    public void addActionSource(MapEventActionWrapperBase source, ActionType actionType){
        ActionSocket socket = new ActionSocket();
        addActionSourceAndSocket(source, socket, actionType);
    }

    public void broadcastReproject(){
        reactTo(ActionTypes.getAction(ActionTypes.ACTION_CLEAR_PROJECTION));
        reactTo(ActionTypes.getAction(ActionTypes.ACTION_USER_MAKE_INITIAL_PROJECTION));
        reactTo(ActionTypes.getAction(ActionTypes.ACTION_USER_PROJECT));
    }
    public void broadcastUpdate(){
        reactTo(ActionTypes.getAction(ActionTypes.ACTION_USER_UPDATE_REQUESTED_ITEMS));
    }

    private boolean mIsPaused;
    private Map<ActionType, MapEventActionWrapperBase> mActionSources;
    private Map<ActionType, IActionSocket> mSockets;
    private AsyncWorker mWorkerThread;
    private Handler mMainThreadHandler;
    /** Actions for handling on a Main thread */
    private Set<ActionType> mProjectorActions;
}
