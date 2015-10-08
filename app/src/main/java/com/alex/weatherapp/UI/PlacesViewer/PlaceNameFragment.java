package com.alex.weatherapp.UI.PlacesViewer;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;

import java.util.ArrayList;
import java.util.List;


public class PlaceNameFragment extends ListFragment {
    public static final  String TAG_FRAGMENT_KIND = "TAG_PLACE_NAME_FRAGMENT";
    private static final String TAG_LOCATIONS = "TAG_LOCATIONS";
    private static final String TAG_STATES = "TAG_STATES";



    public PlaceNameFragment() {
    }

    public static PlaceNameFragment newIntance(IRegistryOfPlaces registryOfPlaces){
        List<LocationData> places = registryOfPlaces.getListOfPlaces();
        List<PlaceUpdateState> states = registryOfPlaces.getPlacesRequestStates();

        Bundle args = new Bundle();
        ArrayList<String> strStates = new ArrayList<>();
        for (PlaceUpdateState state : states){
            strStates.add(state.toString());
        }
        args.putStringArrayList(TAG_STATES, strStates);
        args.putParcelableArrayList(TAG_LOCATIONS, (ArrayList<LocationData>) places);
        PlaceNameFragment fragment = new PlaceNameFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;
        List<String> strStates = args.getStringArrayList(TAG_STATES);
        List<PlaceUpdateState> states = new ArrayList<>();
        for (String s : strStates){
            states.add(PlaceUpdateState.valueOf(s));
        }
        Resources res = getResources();

        List<LocationData> places = args.getParcelableArrayList(TAG_LOCATIONS);
        /** create and attach adapter for ListView */
        mAdapter = new PlaceNameAdapter(getActivity(), places, states);
        /** These values are now references in adapter, so we don't need them here any longer */
        setListAdapter(mAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IHolderInterface)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PlacesViewer.IPlaceSelectedCallbackForList ");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener != null){
            mListener.onPlaceSelected(position);
        }
    }

    public void updateData(List<LocationData> places, List<PlaceUpdateState> states){
        mAdapter = new PlaceNameAdapter(getActivity(),places, states);
        setListAdapter(mAdapter);
    }

    private IHolderInterface mListener;
    PlaceNameAdapter mAdapter;

}
