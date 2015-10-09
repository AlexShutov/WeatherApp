package com.alex.weatherapp.LoadingSystem.CachedLoadingSystem;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastRequest;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationRequest;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationResponse;
import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.LocalStorage.ILocalStorageRequests;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Alex on 17.09.2015.
 */
public class LoadingFacility extends HandlerThread
        implements ILoadingFacility {
    public LoadingFacility(CachedLoadingSystem loadingSystem, Handler callbackThreadHandler) {
        super ("LoadingFacility thread");
        mCallbackThreadHandler = callbackThreadHandler;
        mSys = loadingSystem;
        mCacheStorage = mSys.getCurrentCache();

        // !!!
        mStrategy = null;
        /*
        The removal of item from hash map requires rehashing, tree map is faster here
        */
        mActiveRequests = new TreeMap<>();
        mCurrIDCounter = 0;
    }

    private static final int EXEC_INITIAL_REQUEST_HNDLNG_FROM_STRATEGY = 2;
    private static final int HANDLE_INITIAL_REQUEST = 3;

    private class ControlThreadHandler extends Handler {
        public ControlThreadHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StateOfExecution state = null;
            switch (msg.what) {
                case EXEC_INITIAL_REQUEST_HNDLNG_FROM_STRATEGY:
                    state = (StateOfExecution) msg.obj;
                    if (state.isDone) return;
                    mStrategy.initialRequest(state);
                    break;
                case HANDLE_INITIAL_REQUEST:
                    Pair<RequestAbstract, ICallback> args =
                            (Pair<RequestAbstract, ICallback>) msg.obj;
                    state = createNewStateAndRegisterInTable(args.first, args.second);
                    /** chain with next message responsible for handling this request by strategy */
                    msg = mControlThreadHandler.obtainMessage(EXEC_INITIAL_REQUEST_HNDLNG_FROM_STRATEGY, state);
                    mControlThreadHandler.sendMessage(msg);
                    break;
            }
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mControlThreadHandler = new ControlThreadHandler(getLooper());
    }

    @Override
    public void registerLoadingStrategy(ILoadingStrategy strategy) {
        mStrategy = strategy;
    }
    /**
     * Creates a new RequestState objects and registers it in a table
     * @param request
     * @param callback
     * @return
     */
    public StateOfExecution createNewStateAndRegisterInTable(RequestAbstract request, ICallback callback){
        Integer id = assignID();
            /*Create and fill in a new state object  */
        StateOfExecution newRecState = new StateOfExecution();
        newRecState.requestID = id;
        newRecState.request = request;
        newRecState.callback = callback;
        newRecState.isHandledLocally = false;
        newRecState.localResponse = null;
        boolean typeConfirmed = false;

        if (request instanceof ForecastRequest){
            typeConfirmed = true;
            newRecState.requestType = StateOfExecution.RequestType.ForecastRequest;
        } else
        if (request instanceof LocationRequest) {
            typeConfirmed = true;
            newRecState.requestType = StateOfExecution.RequestType.LocationRequest;
        }
            /* unknown type, ignore this request, something is wrong with RequestInterceptor */
        if (!typeConfirmed) {
            Log.d("CachedLocalSystem: ",
                    "Incoming request of unknown type, check the current RequestInterceptor");
            return new StateOfExecution();
        }
        updateRequestState(newRecState);
        return newRecState;
    }

    @Override
    public void initialRequest(RequestAbstract request, ICallback callback) {
        Pair<RequestAbstract, ICallback> args = new Pair<>(request, callback);
        Message msg = mControlThreadHandler.obtainMessage(HANDLE_INITIAL_REQUEST, args);
        mControlThreadHandler.sendMessage(msg);
    }

    @Override
    public void updateRequestState(StateOfExecution state) {
        synchronized (this) {
            if (!mActiveRequests.containsKey(state.requestID)) {
                if (state.isDone)
                    return;
                mActiveRequests.put(state.requestID, state);
            } else {
                mActiveRequests.remove(state.requestID);
                if (!state.isDone)
                    mActiveRequests.put(state.requestID, state);
            }
        }
    }

    public StateOfExecution getRequestState(Integer stateID){
        StateOfExecution state = null;
        state = mActiveRequests.get(stateID);
        return state;
    }

    @Override
    public ILocalStorageRequests getLocalStorage() {
        return mCacheStorage;
    }

    @Override
    public void doRequestOnNetwork(Integer stateId) {
        INetworkJobExecutor mNetExec = getAssignedLoadingSystem().getNetworkThread();
        StateOfExecution state = getRequestState(stateId);
        if (state == null) return;
        mNetExec.enqueueNetworkJob(state);
    }

    @Override
    public void onNetworkResult(StateOfExecution state) {
        IResponse response = state.networkResponse;
        state.isHandledOnNetwork = true;
        updateRequestState(state);
        mStrategy.onNetworkResponse(state);
    }

    /**
     * Pass corresponding flag from CachedLaodingSystem
     * @return
     */
    @Override
    public boolean doNetworkUpdateWhenPossiible() {
        return getAssignedLoadingSystem().getNetworkUpdateFlag();
    }

    private Integer assignID(){
        int id = mCurrIDCounter++;
        if (id >= ID_TRESHOLD){
            if (!mActiveRequests.containsKey(0)) {
                mCurrIDCounter = 0;
                id = 0;
            }
        }
        return id;
    }

    /**
     * This method is in here only due to poor design and unwillingness to add a local
     * Loading system because a lot of boilerplate classes
     * @param state
     */
    @Override
    public void processLocationRequest(StateOfExecution state) {
        ILocalStorageRequests ls = getLocalStorage();
        LocationRequest req = (LocationRequest) state.request;
        LocationData place = req.getLocationData();
        switch (req.getRequestType()) {
            case ADD_NEW_PLACE:
                ls.addNewPlace(place);
                state.localResponse = new LocationResponse(null);
                break;
            case GET_ONE_PLACE_BY_COORD:
                ls.getOnePlaceByCoordinates(place);
                List<LocationData> ress = new ArrayList<>();
                ress.add(place);
                state.localResponse = new LocationResponse(ress);
                break;
            case GET_ALL_PLACES:
                LocationResponse response = ls.getAllPlaces();
                state.localResponse = response;
                break;

            case REMOVE_PLACE_BY_COORD:
                ls.deletePlaceByCoord(place);
                break;
            case REMOVE_ALL_PLACES:
                ls.dropPlacesTable();
                ls.dropForecastTable();
                break;
            default:
        }
    }

    /**
     * Retrives handler of the thread, callbacks will be posted to
     * @return
     */
    @Override
    public Handler getCallbackThreadHandler() {
        return mCallbackThreadHandler;
    }

    /**
     * retrives handler of this control thread
     * @return
     */
    public Handler getThreadHandler() {
        return mControlThreadHandler;
    }

    public CachedLoadingSystem getAssignedLoadingSystem() {
        return mSys;
    }


    private final Integer ID_TRESHOLD = 200;
    private Integer mCurrIDCounter;
    private Map<Integer, StateOfExecution> mActiveRequests;

    /* top level functionality references , set it up in constructor ,
    isn't final becase CachedLoadingSystem can thange them whenever it wants */
    private CachedLoadingSystem mSys;
    private ILocalStorageRequests mCacheStorage;

    ILoadingStrategy mStrategy;

    private Handler mControlThreadHandler;
    private Handler mCallbackThreadHandler;
}

