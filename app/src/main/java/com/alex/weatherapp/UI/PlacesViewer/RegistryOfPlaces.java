package com.alex.weatherapp.UI.PlacesViewer;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Alex on 06.10.2015.
 */
public class RegistryOfPlaces implements IRegistryOfPlaces {

    public RegistryOfPlaces() {
        reset();
    }

    @Override
    public void reset() {
        sRequestsCounter = 0;
        mPlaceStateData = new TreeMap<>();
        mPlaceArrivalOrder = new TreeMap<>();
    }

    @Override
    public void addPlace(LocationData place) {
        PlaceUpdateState newState = PlaceUpdateState.NameOnly;
        /** we're receiving place the first time, assign it a row number */
        if (!mPlaceStateData.containsKey(place)){
            mPlaceArrivalOrder.put(sRequestsCounter++, place);
        }else {
            //* Ouch! by some reason we encountered the same place the second time */
            mPlaceStateData.remove(place);
        }
        mPlaceStateData.put(place, newState);
    }

    @Override
    public void addPlaces(List<LocationData> listOfPlaces) throws IllegalArgumentException {
        if (listOfPlaces == null || listOfPlaces.isEmpty()){
            throw new IllegalArgumentException("List of places is empty");
        }
        for (LocationData l : listOfPlaces){
            addPlace(l);
        }
    }

    @Override
    public void addPlace(LocationData place, PlaceUpdateState placeState) {
        if (!mPlaceStateData.containsKey(place)){
            mPlaceArrivalOrder.put(sRequestsCounter++, place);
        }else {
            //* Ouch! by some reason we encountered the same place the second time */
            mPlaceStateData.remove(place);
        }
        mPlaceStateData.put(place, placeState);
    }

    @Override
    public boolean isHasPlace(LocationData place) {
        return mPlaceStateData.containsKey(place);
    }

    @Override
    public void processPlaceRequest(LocationData placeRequestCameFor) {
        if (!mPlaceStateData.containsKey(placeRequestCameFor)){
            addPlace(placeRequestCameFor);
            return;
        }
        PlaceUpdateState currState = mPlaceStateData.get(placeRequestCameFor);
        PlaceUpdateState newState = PlaceUpdateState.NameOnly;
        boolean modified = true;
        switch (currState){
            case NameOnly:
                newState = PlaceUpdateState.ProcessedCache;
                break;
            case ProcessedCache:
                newState = PlaceUpdateState.ProcessedOnline;
                break;
            case ProcessedOnline:
                newState = PlaceUpdateState.ProcessedOnline;
                modified = false;
                break;
            default:
                modified = false;
                break;
        }
        if (modified) {
            mPlaceStateData.remove(placeRequestCameFor);
            mPlaceStateData.put(placeRequestCameFor, newState);
        }
    }

    @Override
    public void processPlaceRequestThrow(LocationData placeRequestCameFor) throws IllegalArgumentException {
        if (!isHasPlace(placeRequestCameFor)){
            throw new IllegalArgumentException("Registry has no such a place");
        }
        processPlaceRequest(placeRequestCameFor);
    }

    @Override
    public PlaceUpdateState getPlaceState(LocationData place) throws IllegalArgumentException {
        if (!mPlaceStateData.containsKey(place)){
            throw new IllegalArgumentException("Registry has no requested place in it");
        }
        return mPlaceStateData.get(place);
    }

    @Override
    public LocationData getPlaceByEncounter(int position) throws IndexOutOfBoundsException{
        int size = mPlaceArrivalOrder.size();
        if (position >= size){
            throw new IndexOutOfBoundsException("List only has " + size + "places, but " +
            position + "'s is requested");
        }
        return mPlaceArrivalOrder.get(position);
    }

    @Override
    public void setPlaceState(LocationData place, PlaceUpdateState requestState) throws
            IllegalArgumentException {
        if (!mPlaceStateData.containsKey(place)){
            throw new IllegalArgumentException("Registry has no requested place in it");
        }
        mPlaceStateData.remove(place);
        mPlaceStateData.put(place, requestState);
    }

    @Override
    public List<LocationData> getListOfPlaces() {
        List<LocationData> resultList = new ArrayList<>();
        int nPlaces = mPlaceArrivalOrder.size();
        for (int i = 0; i < nPlaces; ++i){
            LocationData l = mPlaceArrivalOrder.get(i);
            LocationData nl = new LocationData(l);
            resultList.add(nl);
        }
        return resultList;
    }

    @Override
    public List<PlaceUpdateState> getPlacesRequestStates() {
        List<PlaceUpdateState> resultList = new ArrayList<>();
        int nPlaces = mPlaceArrivalOrder.size();
        LocationData tmp = null;
        for (int i = 0; i < nPlaces; ++i){
            tmp = mPlaceArrivalOrder.get(i);
            PlaceUpdateState state = mPlaceStateData.get(tmp);
            resultList.add(state);
        }
        return resultList;
    }

    @Override
    public int getPlaceEncounterNo(LocationData place) throws IllegalArgumentException {
        for (Integer i : mPlaceArrivalOrder.keySet()){
            LocationData tmp = mPlaceArrivalOrder.get(i);
            if (tmp.equals(place))
                return i;
        }
        throw new IllegalArgumentException("value not found");
    }

    @Override
    public boolean removePlace(LocationData place) {
        if (!mPlaceStateData.containsKey(place)){
            return false;
        }
        mPlaceStateData.remove(place);
        int placePos = 0;
        for (Integer i : mPlaceArrivalOrder.keySet()){
            if (mPlaceArrivalOrder.get(i) == place){
                placePos = i;
                break;
            }
        }
        /** copy values from 0..i-1 and i+1, ..N */
        Map<Integer, LocationData> newOrder = new TreeMap<>();
        for (int i = 0; i < placePos; ++i){
            newOrder.put(i, mPlaceArrivalOrder.get(i));
        }
        int N = mPlaceArrivalOrder.size();
        if (placePos + 1 != N){
            for (int i = placePos + 1; i < N; ++i) {
                newOrder.put( i-1, mPlaceArrivalOrder.get(i));
            }
        }
        mPlaceArrivalOrder = newOrder;
        return true;
    }

    /** increment this counter when adding a new place */
    private static int sRequestsCounter;

    /** Stores inormation about request processing state */
    private Map<LocationData, PlaceUpdateState> mPlaceStateData;
    /** Order of place arrival- position of place in ListView */
    private Map<Integer, LocationData> mPlaceArrivalOrder;

}
