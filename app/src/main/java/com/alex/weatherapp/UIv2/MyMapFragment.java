package com.alex.weatherapp.UIv2;

import android.app.Activity;
import android.os.Bundle;

import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by Alex on 30.11.2015.
 */
public class MyMapFragment extends MapFragment {
    public static final String TAG_MY_MAP_FRAGMENT = "MY_MAP_FRAGMENT";
    public interface IOnMapReady{
        void acceptMapInstance(GoogleMap map);
    }
    public MyMapFragment(){
        map = null;
        activity = null;
        isMapSentToHolder = false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        if (!(activity instanceof IOnMapReady)){
            throw new IllegalStateException("Activity must implement IOnMapReady");
        }
        sendMapToActivity();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                Logger.w("Map instance is acquired");
                if (null != activity){
                    sendMapToActivity();
                }
            }
        });
    }

    private void sendMapToActivity(){
        if (null != map && !isMapSentToHolder){
            ((IOnMapReady)activity).acceptMapInstance(map);
            isMapSentToHolder = true;
            activity = null;
        }
    }

    private GoogleMap map;
    private boolean isMapSentToHolder;
    private Activity activity;
}
