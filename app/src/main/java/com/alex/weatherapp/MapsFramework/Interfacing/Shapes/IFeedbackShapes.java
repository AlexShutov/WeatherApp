package com.alex.weatherapp.MapsFramework.Interfacing.Shapes;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.Interfacing.IFeedbackInterface;
import com.alex.weatherapp.MapsFramework.MapVisuals.Markers.PlaceData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.CircularRegionData;
import com.alex.weatherapp.MapsFramework.MapVisuals.Shapes.RectRegionData;

/**
 * Created by Alex on 14.11.2015.
 */

/**
 * Weather map feedback interface
 */
public interface IFeedbackShapes extends IFeedbackInterface {
    void onCircularRegionSelected(CircularRegionData data);
    void onRectRegionSelected(RectRegionData data);
    void onNothingSelected();

    void onNewPlacePinned(LocationData place);
    void onInfoMarkerClick(PlaceData infoMarker);


    void showServiceMessage(String msg);
}
