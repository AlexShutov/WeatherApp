package com.alex.weatherapp.UIDynamic;

import android.app.Activity;
import android.os.Bundle;

import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
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
    public static MyMapFragment newInstance(GoogleMapOptions options) {
        MyMapFragment var1 = new MyMapFragment();
        Bundle var2 = new Bundle();
        var2.putParcelable("MapOptions", options);
        var1.setArguments(var2);
        return var1;
    }
    public MyMapFragment(){
        super();
        map = null;
        activity = null;
        isMapSentToHolder = false;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.w("TRACE: MyMapFragment: onActivityCreated()");
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
            Logger.w("TRACE: MyMapFragment: sendMapToActivity()");
            ((IOnMapReady)activity).acceptMapInstance(map);
            isMapSentToHolder = true;
            activity = null;
            map = null;
        }
    }

    private GoogleMap map;
    private boolean isMapSentToHolder;
    private Activity activity;
}
