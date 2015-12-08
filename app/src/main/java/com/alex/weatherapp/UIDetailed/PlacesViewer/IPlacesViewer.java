package com.alex.weatherapp.UIDetailed.PlacesViewer;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

import java.util.List;

/**
 * Created by Alex on 06.10.2015.
 */

/**
 * It may be confusing, that ForecastViewer implements IPlaceSelectedCallback interface,
 * but it is done for providing feedback between PlacesViewer and fragment, showing place list.
 * Its implementation must notify top-hand entity about that event.
 */
public interface IPlacesViewer extends IHolderInterface {

    interface IPlaceSelectedCallback {
        void onPlaceSelected(LocationData selectedPlace);
        /* Notifies upper entity that there is no saved places and user touched area to add
        a new place
         */
        void onNoPlaceUpdateRequested();
    }

    /** Clear places list and its handling states */
    void reset();

    /**
     * Tell the PlacesViewer, which frame layout to place fragment to.
     * @param frameResourceID
     */
    void setFrameResourceID(int frameResourceID);

    /** Set notifier, firing when place is selected. This callback gets actual place,
     * not its index in ListView
     * @param placeSelectedCallback
     */
    void setSelectedCallback(IPlaceSelectedCallback placeSelectedCallback);

    /**
     * If set, viewer will not invalidate ListView, just update its data. This method is
     * essential for single-pane processing, when user observes another forecast and we
     * receives an update about other place, so we'll just save it. It just terminates
     * invalidating of a view.
     * @param isOn
     */
    void turnOnSilentMode(boolean isOn);
    /**
     * PlacesViewer stores list of places along with loading data in registry of places
     * (HashMap), so this method will replace existing data by new, which loading state is
     * 'not loaded'. If registry has no such a record, it will be added into it with state
     * 'not loaded'. Registry also has information about order of places another
     * Map<LocationData, int>, whereas int is a order of arrival. When new not existing place
     * arrives, it gets added to the end of a list.
     * @param placesToShow
     */
    void showPlaceList(List<LocationData> placesToShow);

    /** Call this method on IModel responce arrival. Place viewer watches response state for every
     * registered place. Viewer will update request state for this place and change color of
     * that place in ListView.
     * @param placeToUpdate
     */
    void processIncomingResponse(LocationData placeToUpdate, boolean addNotExisting);

    // remove place from list view
    void removePlace(LocationData place);

    int getPlaceListPostion(LocationData place) throws IllegalArgumentException;
    LocationData getPlaceByPosition(int position);

    void saveState(LocationData pickedLocation);
    public LocationData restoreState();

    void invalidate();

}
