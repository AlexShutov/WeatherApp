package com.alex.weatherapp.UI.PlacesViewer;

/**
 * Created by Alex on 06.10.2015.
 */


/**
 * This iface is responsible for feedback between viewing fragment and PlacesViewer.
 * This interface must be implemented in calling activity, its implementation redirects call
 * to IPlaceViewer. It is necessary for being consistent with Activity-Fragment interaction.
 * ListFragment calls it when user select the place. If there are no places,
 * onEmptyViewClicked() is called. Assume, user has no saved places, so we show a fragment
 * 'touch to add a new place', in that case that fragment call the former method, which get to
 *
 */

public interface IHolderInterface {
    void onPlaceSelected(int viewPosition);
    void onEmptyViewClicked();
}


