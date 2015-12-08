package com.alex.weatherapp.UIDetailed;

import android.app.Activity;

import com.alex.weatherapp.UIDetailed.PlaceForecastViewer.IForecastViewer;
import com.alex.weatherapp.UIDetailed.PlacesViewer.IHolderInterface;

/**
 * Created by Alex on 07.10.2015.
 */
/**
 * Indicates, that hoding activity must implement holder interfaces of both list viewer and
 * forecast viewer
 */
interface ILinkToHolderActivity extends
        IForecastViewer.IHolderInterface,
        IHolderInterface {
    /** Retrives activity instance for configurinf viewers */
    Activity getActivity();

    void onNoPlacesUpdate();
}