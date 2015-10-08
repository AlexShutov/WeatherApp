package com.alex.weatherapp.UI.PlacesViewer;

import android.content.res.Resources;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

import java.util.List;

/**
 * Created by Alex on 06.10.2015.
 */
public interface IRegistryOfPlaces {
    void reset();
    void addPlace(LocationData place);
    void addPlaces(List<LocationData> listOfPlaces) throws IllegalArgumentException;
    void addPlace(LocationData place, PlaceUpdateState placeState);

    boolean isHasPlace(LocationData place);

    /** Gradually alters PlaceUpdateState for given place NameOnly->Cached->Online
     * It is neccessary, because local responces come first, after them Online lookup results.
     * In case when placeRequestCameFor not belongs to registry, new place is added.
     * The second method (with Throw suffix) doesn't add new request and throws an exception
     * if place isn't in registry
     * @param placeRequestCameFor
     */
    void processPlaceRequest(LocationData placeRequestCameFor);
    void processPlaceRequestThrow(LocationData placeRequestCameFor) throws IllegalArgumentException;

    /**
     * @return Retrives the list of places in order they were added
     */
    List<LocationData> getListOfPlaces();

    /**
     * Finds Location of place under position 'position' and returns it. Useful when
     * notifying higher-level entity about click, because it needs to know a place location,
     * not its order in a list.
     * @param position
     * @return
     * @throws IndexOutOfBoundsException
     */
    LocationData getPlaceByEncounter(int position) throws IndexOutOfBoundsException;

    /**
     * It may come handy in displaying places in ListView. For example, paint all updated places
     * in green.
     * @return Retrives states for each place.
     */
    List<PlaceUpdateState> getPlacesRequestStates();


    /**
     * Retrives request state for a given place, exception is thrown if registry has no
     * such a place.
     * @param place
     * @return
     * @throws IllegalArgumentException
     */
    PlaceUpdateState getPlaceState(LocationData place) throws IllegalArgumentException;

    /** Forces setting request's state for a place 'place'.
     * @param place
     * @param requestState
     * @throws IllegalArgumentException Is thrown if registry has no place 'place'
     */
    void setPlaceState(LocationData place, PlaceUpdateState requestState) throws
            IllegalArgumentException;


    /**
     * Retrives position, place were fist encountered by
     * @param place
     * @return
     * @throws IllegalArgumentException
     */
    int getPlaceEncounterNo(LocationData place) throws IllegalArgumentException;

    /**
     * remove place from registry
     * @param place
     * @return true on success
     */
    boolean removePlace(LocationData place);
}
