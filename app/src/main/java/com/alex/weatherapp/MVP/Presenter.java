package com.alex.weatherapp.MVP;

import android.util.Log;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.ILoadingFacade;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;

import java.util.List;

/**
 * Created by Alex on 03.10.2015.
 */
public class Presenter extends PresenterBase {
    public Presenter(){
        super();
    }

    @Override
    public void onModelConnected() {
        Log.d("Presenter", "void onModelConnected()");
    }

    @Override
    public void onModelDisconnected() {
        Log.d("Presenter", "void onModelDisconnected()");
    }


    /**
     * IPresenterContract implementation. Each of these method just redirects call to model
     * and handles asynchronous result retrieval. Model and Presenter are supposed to be managed
     * by MVPManager (WeatherApplication) and model with its underlying service and thread will
     * be stopped before the presenter, that's why here is no need to use WeakReference for
     * avoiding memory leaks
     */

    @Override
    public void getListOfSavedPlaces() {
        Log.d("Presenter", "void getListOfSavedPlaces()");
        /** We can't execut any requests if model isn't ready, that line of code is in every
         * IPresenterContract method */
        if (!isModelReady()) {
            return;
        }
        mModelContract.getAllLocations(new ILoadingFacade.IOnLocationsLoadedCallback() {
            @Override
            public void onCompletion(List<LocationData> locations) {
                Log.d("Presenter", "List of places is loaded");
                if (!isViewReady()) return;
                mAttachedView.handleListOfSavedPlaces(locations);
            }
        });
    }

    @Override
    public void acquireForecastsForAllPlaces() {
        Log.d("Presenter", "void acquireForecastsForAllPlaces()");
       if (!isModelReady()) {
            return;
        }
        ILoadingFacade.IAllForecastsRetriever fetcher = mModelContract.getDataRetriever();
        fetcher.setEachForecastCallback(new ILoadingFacade.IOnLocationForecast() {
            @Override
            public void onResult(LocationData place, Forecast forecast) {
                PlaceForecast f = new PlaceForecast(place, forecast);
                if (!isViewReady()) return;
                mAttachedView.showPlaceForecast(f);
            }
        });
        fetcher.setFinalCallback(new ILoadingFacade.IOnPlaceForecastsLoaded() {
            @Override
            public void getPlaceForecasts(List<PlaceForecast> placeForecasts) {
                if (!isViewReady()) return;
                mAttachedView.showPlacesForecasts(placeForecasts);
            }
        });
        fetcher.begin();
    }

    @Override
    public void getStandAlonePlaceForecast(LocationData placeCoord) {
        Log.d("Presenter", "void getStandAlonePlaceForecast(LocationData placeCoord)");
        if (!isModelReady()) {
            return;
        }
        mModelContract.getForecastForLocation(placeCoord,
                new ILoadingFacade.IOnLocationForecast() {
                    @Override
                    public void onResult(LocationData place, Forecast forecast) {
                        if (!isViewReady()) return;
                        PlaceForecast f = new PlaceForecast(place, forecast);
                        mAttachedView.showStandalonePlaceForecast(f);
                    }
                });
    }

    @Override
    public void acquirePlaceForecast(LocationData placeCoords) {

    }

    @Override
    public void getForecastOnlineNoCache(LocationData placeCoords) {
        Log.d("Presenter", "void getForecastOnlineNoCache(LocationData placeCoords)");
        if (!isModelReady()) {
            return;
        }
        mModelContract.getForecastForLocationOnlineNoCache(placeCoords,
                new ILoadingFacade.IOnLocationForecast() {
            @Override
            public void onResult(LocationData place, Forecast forecast) {
                if (!isViewReady()) return;
                PlaceForecast f = new PlaceForecast(place, forecast);
                mAttachedView.showPlaceForecast(f);
            }
        });
    }

    @Override
    public void setForceNetworkUpdate(boolean forceUpdate) {
        Log.d("Presenter", "void setForceNetworkUpdate(boolean forceUpdate)");
        if (!isModelReady()) {
            return;
        }
        mModelContract.setAlwaysUpdate(forceUpdate);
    }


    @Override
    public void addNewPlace(final LocationData placeInfo) {
        Log.d("Presenter", "void addNewPlace(LocationData placeInfo)");
        if (!isModelReady()) {
            return;
        }
        final LocationData p = placeInfo;
        mModelContract.addNewPlace(p.getLat(), p.getLon(), p.getmPlaceName(),
                new ILoadingFacade.IEmptyCallback() {
                    @Override
                    public void onCompletion() {
                        if(!isViewReady()) return;
                        mAttachedView.onNewPlaceIsAddedToPlaceRegistry(p);
                    }
                });
    }

    @Override
    public void removeAllPlaces() {
        Log.d("Presenter", "void addNewPlace(LocationData placeInfo)");
        if (!isModelReady()) {
            return;
        }
        mModelContract.removeAllPlcaes(new ILoadingFacade.IEmptyCallback() {
            @Override
            public void onCompletion() {
                if (!isViewReady()) return;
                mAttachedView.onAllPlacesRemoved();
            }
        });
    }

    @Override
    public void getGoogleGeolookup(LocationData placeCoords) {

    }
}
