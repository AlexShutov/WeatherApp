package com.alex.weatherapp.LoadingSystem;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.alex.weatherapp.LoadingSystem.CachedLoadingSystem.CachedLoadingSystem;
import com.alex.weatherapp.LoadingSystem.CachedLoadingSystem.ILoadingStrategy;
import com.alex.weatherapp.LoadingSystem.CachedLoadingSystem.LoadingStrategy;
import com.alex.weatherapp.LoadingSystem.CachedLoadingSystem.LoadingStrategyOffline;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastRequest;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastResponse;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationRequest;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationResponse;
import com.alex.weatherapp.LoadingSystem.LocalStorage.ILocalStorageRequests;
import com.alex.weatherapp.LoadingSystem.NetworkStateListener.INetStateListenerControl;
import com.alex.weatherapp.LoadingSystem.NetworkStateListener.INetStateListenerFeedback;
import com.alex.weatherapp.LoadingSystem.NetworkStateListener.NetworkStateListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Alex on 22.09.2015.
 */
public class Facade4LoadingSystem implements ILoadingFacade, INetStateListenerFeedback {

    private static class ModeSwitchState {
        public ModeSwitchState(){
            isOffline = true;
        }
        /** TODO: now network just returns null and is ignored, but to that point almost all
         * TODO: local data is being retrived, because db works much faster */
        public boolean switchedDuringExecution;
        public boolean goneOffline;
        public boolean isOffline;
    };

    public static class Builder {
        public Builder() {
            mContext = null;
            mNetworkLS = null;
            mCache = null;
        }
        public Builder setContext(Context context){
            mContext = context;
            return this;
        }
        public Builder setNetworkLOadingSystem(LoadingSystem mNetLS){
            mNetworkLS = mNetLS;
            return this;
        }
        public Builder setLocalCache(ILocalStorageRequests cache){
            mCache = cache;
            return this;
        }
        public Facade4LoadingSystem build(){
            if (mContext == null || mCache == null || mNetworkLS == null) {
                throw new RuntimeException("One of the components is null, terminating");
            }
            return new Facade4LoadingSystem(mContext, mNetworkLS, mCache);
        }

        private Context mContext;
        private LoadingSystem mNetworkLS;
        private ILocalStorageRequests mCache;
    }

    public Facade4LoadingSystem(Context context, LoadingSystem networkLS,
                                ILocalStorageRequests cache) {
        mContext = context;
        mCachedLS = new LoadingStrategy();
        mOfflineLS = new LoadingStrategyOffline();
        mNetworkLS = networkLS;
        mCache = cache;
        mUIHandler = new Handler(Looper.getMainLooper());
        mLSys = new CachedLoadingSystem(mNetworkLS, mCache, mUIHandler);
        /* Configure mode state */
        mModeSwitchState = new ModeSwitchState();
        mModeSwitchState.isOffline = true;
        mModeSwitchState.goneOffline = false;
        mModeSwitchState.switchedDuringExecution = false;
        goOffline();

        /* configure and fire network state listener */
        mNetStateListener = new NetworkStateListener(mContext);
        mNetStateListener.setFeedback(this);
        mNetStateListener.startListening();
        mNetStateListener.forceStateChecking();
    }

    public ForecastForAllPlacesRetriever getDataRetriever() {
        return this.new ForecastForAllPlacesRetriever();
    }
    public class ForecastForAllPlacesRetriever implements IAllForecastsRetriever  {

        protected ForecastForAllPlacesRetriever(){
            mFinalCallback = null;
            mEachForecastCallback = null;
            mCallbackCntTarget = 0;
        }
        @Override
        public void begin() {
            mData = new ArrayList<>();
            mCallbackCntTarget = 0;
            mEncounterFlags = new TreeMap<>();
            mForecasts = new TreeMap<>();
            getAllLocations(this);
        }

        @Override
        public void onCompletion(List<LocationData> locations) {
            mCallbackCntTarget = locations.size();
            for (LocationData l : locations) {
                getForecastForLocation(l, this);
            }
        }

        @Override
        public void onResult(LocationData place, Forecast forecast) {
            mCallbackCnt++;
            /* first encounter, perhaps result came from cache */
            if (!mEncounterFlags.containsKey(place)) {
                mEncounterFlags.put(place, true);
                mForecasts.put(place, forecast);
            } else {
                /* we already have forecast for the place, update it with a newer version */
                mForecasts.remove(place);
                mForecasts.put(place, forecast);
            }
            /* If caller is alive, inform it */
            IOnLocationForecast cb = null;
            if (mEachForecastCallback != null) {
                cb = mEachForecastCallback.get();
            }
            if (cb != null) {
                cb.onResult(place, forecast);
            }
            if (isOffline() && mCallbackCnt == mCallbackCntTarget ||
                    !isOffline() && mCallbackCnt == 2 * mCallbackCntTarget) {
                IOnPlaceForecastsLoaded finalCallback  =  null;
                if (mFinalCallback != null ){
                    finalCallback = mFinalCallback.get();
                }
                if (finalCallback == null) return;
                List<PlaceForecast> res = new ArrayList<>();
                for (Map.Entry<LocationData, Forecast> e : mForecasts.entrySet()) {
                    PlaceForecast pf = new PlaceForecast(e.getKey(), e.getValue());
                    res.add(pf);
                }
                finalCallback.getPlaceForecasts(res);
            }
        }
        @Override
        public void setEachForecastCallback(IOnLocationForecast callback) {
            if (callback == null) {
                mEachForecastCallback = null;
                return;
            }
            mEachForecastCallback = new WeakReference<IOnLocationForecast>(callback);
        }
        @Override
        public void setFinalCallback(IOnPlaceForecastsLoaded callback) {
            if (callback == null) {
                mFinalCallback = null;
                return;
            }
            mFinalCallback = new WeakReference<IOnPlaceForecastsLoaded>(callback);
        }
        private List<PlaceForecast> mData;
        /**
         * Caller is notified as about data from local cache, as network response.
         * We only need to keep the relevant data (network) or local, if there are no
         * network data. To achieve that we have a flag array- true if we\re already encountered
         * that location. If not, save it. If data is in the array, replace it by the newer version
         */

        int mCallbackCntTarget;
        int mCallbackCnt;
        private Map<LocationData, Boolean> mEncounterFlags;
        private Map<LocationData, Forecast> mForecasts;
        private WeakReference<IOnPlaceForecastsLoaded> mFinalCallback;
        private WeakReference<IOnLocationForecast> mEachForecastCallback;
    }

    @Override
    public void onOffline() {
        this.goOffline();
    }

    @Override
    public void onOnline() {
        this.goOnlineWithCache();
    }

    @Override
    public void onWiFiAvailible() {
        /*TODO: Have nothing to do in here right now */
    }

    @Override
    public void onCellularAvailible() {
         /*TODO: Have nothing to do in here right now */
    }


    public void goOffline() {
        mModeSwitchState.isOffline = true;
        mLSys.switchExecutionStrategy(mOfflineLS);
    }
    public void goOnlineWithCache() {
        mModeSwitchState.isOffline = false;
        mLSys.switchExecutionStrategy(mCachedLS);
    }
    public boolean isOffline() { return mModeSwitchState.isOffline;}

    public void terminateThis(){
        mNetStateListener.stopListening();
        mLSys.shutdownNow();
    }

    /**
     * Service must have no direct references to avoid memory leaks, because
     * screen may be destroyed when minimized, but would be kept in memory.
     * If so, corrupt action must be silently ignored.
     */
    private static class WeakCallbackDecorator implements ICallback {
        public WeakCallbackDecorator(ICallback wrappedCb) {
            mWrappedCalback = new WeakReference<ICallback>(wrappedCb);
        }

        @Override
        public void onResult(IResponse response) {
            ICallback callMe = mWrappedCalback.get();
            if (callMe == null) return;
            callMe.onResult(response);
        }
        private WeakReference<ICallback> mWrappedCalback;
    }
    /**
     * It is better to avoid casting from IResult, so simple callbacks and
     * their wrappers has been introduced
     */
    private static class EmptyCallbackAdapter implements ICallback {
        public EmptyCallbackAdapter(IEmptyCallback ecb){
            mEmptyCallback = ecb;
        }
        @Override
        public void onResult(IResponse response) {
            mEmptyCallback.onCompletion();
        }
        private IEmptyCallback mEmptyCallback;
    };
    @Override
    public void addNewPlace(double lat, double lon, String name, IEmptyCallback callback) {
        EmptyCallbackAdapter w = new EmptyCallbackAdapter(callback);
        WeakCallbackDecorator w2 = new WeakCallbackDecorator(w);
        LocationData place = new LocationData(lat,lon, name);
        LocationRequest lr = new LocationRequest(place, LocationRequest.RequestType.ADD_NEW_PLACE);
        mLSys.execute(lr, w2);
    }
    private static class IOnLocationsLoadedCallbackAdapter implements ICallback {
        public IOnLocationsLoadedCallbackAdapter(IOnLocationsLoadedCallback cb){
            mWrapperCb = cb;
        }

        @Override
        public void onResult(IResponse response) {
            if (!(response instanceof LocationResponse)) return;
            LocationResponse r = (LocationResponse) response;
            mWrapperCb.onCompletion(r.getLocations());
        }
        IOnLocationsLoadedCallback mWrapperCb;
    }
    @Override
    public void getAllLocations(IOnLocationsLoadedCallback callback){
        LocationRequest lr = new LocationRequest(null, LocationRequest.RequestType.GET_ALL_PLACES);
        IOnLocationsLoadedCallbackAdapter cbAdapter = new IOnLocationsLoadedCallbackAdapter(callback);
        ICallback cb = new WeakCallbackDecorator(cbAdapter);
        mLSys.execute(lr, cb);
    }
    @Override
    public void removeAllPlcaes(IEmptyCallback callback) {
        LocationRequest lr = new LocationRequest(new LocationData(0, 0),
                LocationRequest.RequestType.REMOVE_ALL_PLACES);
        ICallback ecb = new WeakCallbackDecorator(new EmptyCallbackAdapter(callback));
        mLSys.execute(lr, ecb);
    }

    /**
     * This methods is rarely used, so it has no standalone callback wrapper
     * @param latitude
     * @param longitude
     * @param callback
     */
    @Override
    public void getLocationName(double latitude, double longitude, ICallback callback){
        LocationData coords = new LocationData(latitude, longitude);
        LocationRequest lr = new LocationRequest(coords, LocationRequest.RequestType.GET_ONE_PLACE_BY_COORD);
        mLSys.execute(lr, callback);
    }
    private class OnLocationForecastAdapter implements ICallback {
        public OnLocationForecastAdapter(IOnLocationForecast adaptedCallback, LocationData place) {
            mAdaptedCallback = adaptedCallback;
            mPlace = new WeakReference<LocationData>(place);
        }
        @Override
        public void onResult(IResponse response) {
            if(!(response instanceof ForecastResponse)) return;
            /* return if screen were deleted */
            if(mPlace.get() == null) return;
            ForecastResponse r = (ForecastResponse) response;
            Forecast f = (Forecast)r.getForecastData();
            mAdaptedCallback.onResult(getPlace(), f);
        }
        public void setPlace(LocationData place) { mPlace = new WeakReference<LocationData>(place);}
        public LocationData getPlace(){ return mPlace.get();}
        WeakReference<LocationData> mPlace;
        IOnLocationForecast mAdaptedCallback;
    }
    @Override
    public void getForecastForLocation(LocationData place, IOnLocationForecast callback) {
        ICallback cb = new OnLocationForecastAdapter(callback, place );
        ICallback wcb = new WeakCallbackDecorator(cb);
        ForecastRequest req = new ForecastRequest(place.getLat(), place.getLon());
        mLSys.execute(req, wcb);
    }

    /**
     *Almost the same as the previous method, the only difference is that in here the flag
     * onlineNoCache is set in request, and it's up to system to handle that
     * @param place
     * @param callback
     */
    @Override
    public void getForecastForLocationOnlineNoCache(LocationData place, IOnLocationForecast callback) {
        ICallback cb = new OnLocationForecastAdapter(callback, place );
        ICallback wcb = new WeakCallbackDecorator(cb);
        ForecastRequest req = new ForecastRequest(place.getLat(), place.getLon());
        req.setOnlineNoCache(true);
        mLSys.execute(req, wcb);
    }

    @Override
    public void setAlwaysUpdate(boolean alwaysUpdate) {
        mLSys.setNetworkUpdateFlag(alwaysUpdate);
    }

    private Context mContext;
    private ILoadingStrategy mCachedLS;
    private ILoadingStrategy mOfflineLS;

    private ModeSwitchState mModeSwitchState;

    private CachedLoadingSystem mLSys;
    private LoadingSystem mNetworkLS;
    private ILocalStorageRequests mCache;
    private INetStateListenerControl mNetStateListener;
    private Handler mUIHandler;

}
