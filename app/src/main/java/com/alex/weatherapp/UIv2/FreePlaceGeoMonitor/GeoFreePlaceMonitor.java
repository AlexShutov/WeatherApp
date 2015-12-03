package com.alex.weatherapp.UIv2.FreePlaceGeoMonitor;

import android.content.Context;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.UIv2.IViewingController;

/**
 * Created by Alex on 03.12.2015.
 */
public class GeoFreePlaceMonitor implements IFreePlaceMonitor {
    public GeoFreePlaceMonitor(Context c){
        context = c;
    }

    @Override
    public void acceptNewFreePlace(LocationData place) {
        Toast.makeText(context, "Free place is selected: " + place.getmPlaceName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSavedPlaceSelected() {
        Toast.makeText(context, "Saved place were selected",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void assignViewingController(IViewingController viewingController) {
    }

    private Context context;
}
