package com.alex.weatherapp.UI.PlacesViewer;

/**
 * Created by Alex on 06.10.2015.
 */

/** Place has few states. It is necessary for displaying forecast data. For example, suppose,
 *  program works in OnlineOnly mode. We already acquired list of all saved places from local
 *  storage, so we show them. But there are no data to show when user select that place yet.
 *  To solve that problem this enum were introduce. Registry support place update state, and if
 *  there is no data, touch event doues nothing.
 */
public enum PlaceUpdateState {
    NameOnly,
    ProcessedCache,
    ProcessedOnline
}
