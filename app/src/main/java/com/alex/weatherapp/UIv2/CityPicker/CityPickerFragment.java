package com.alex.weatherapp.UIv2.CityPicker;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.MapsFramework.BehaviourRelated.Action;
import com.alex.weatherapp.R;
import com.alex.weatherapp.Utils.Logger;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CityPickerFragment extends Fragment {
    public static final String CITY_PICKER_FRAGMENT = "CITY_PICKER_FRAGMENT";
    private static final String CITIES_ARGUMENT = "list_of_cities";
    private static final String CITIES_SELECTED = "city_seleced";

    private class LocationAdapter extends ArrayAdapter<LocationData>{
        public LocationAdapter(Context context, int resource) {
            super(context, resource);
        }

        public LocationAdapter(Context context, int resource, List<LocationData> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LocationData place = getItem(position);
            String placeName = place.getmPlaceName();
            View v = super.getView(position, convertView, parent);
            TextView tv = (TextView) v;
            tv.setText(placeName);
            return v;
        }

        /**
         * andoid.R.layout.simple_spinner_dropdown_item is just TextView under the hood, the same
         * as .._dropdown_item
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            LocationData loc = getItem(position);
            View v = super.getDropDownView(position, convertView, parent);
            TextView tv = (TextView) v;
            tv.setText(loc.getmPlaceName());
            return v;
        }
    }

    public static CityPickerFragment newInstance(ArrayList<LocationData> cities){
        Bundle args = new Bundle();
        args.putParcelableArrayList(CITIES_ARGUMENT, cities);
        CityPickerFragment instance = new CityPickerFragment();
        instance.setArguments(args);
        return instance;
    }
    public static CityPickerFragment newInstance(ArrayList<LocationData> cities,
                                                 LocationData selected){
        Bundle args = new Bundle();
        args.putParcelableArrayList(CITIES_ARGUMENT, cities);
        if (null != selected) {
            args.putParcelable(CITIES_SELECTED, selected);
        }
        CityPickerFragment instance = new CityPickerFragment();
        instance.setArguments(args);
        return instance;
    }

    public LocationData getPicked() throws IllegalStateException{
        LocationData picked = null;
        try{
            picked = (LocationData) mPicker.getSelectedItem();
        }catch (Exception e){
            throw new IllegalStateException(e.getCause());
        }
        return picked;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        mOwner = (ICityPickerChannel) activity;
    }

    @Override
    public void onDetach() {
        mOwner = null;
        super.onDetach();
    }

    public CityPickerFragment() {
        mOwner = null;
        mPicker = null;
        mSelected = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args =  getArguments();
        ArrayList<LocationData> cities = args.getParcelableArrayList(CITIES_ARGUMENT);
        LocationData selected = args.getParcelable(CITIES_SELECTED);
        mCitiesToShow = new ArrayList<>();
        mCitiesToShow.addAll(cities);

   }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.city_picker_fragment_layout, container, false);
        ArrayList<String> placeNames = new ArrayList<>();

        mAdapter = new LocationAdapter(getActivity(),
                android.R.layout.simple_spinner_item,
                mCitiesToShow);

        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner cityPicker = (Spinner) view.findViewById(R.id.idc_cpf_sp_city);
        cityPicker.setAdapter(mAdapter);
        cityPicker.setPrompt("choose the city");
        cityPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.i("Item selected: " + mAdapter.getItem(position).getmPlaceName());
                if (null != mOwner) {
                    LocationData pickedPlace = mAdapter.getItem(position);
                    mSelected = pickedPlace;
                    mOwner.cityPicked(pickedPlace);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Logger.i("Nothing is selected");
                mSelected = null;
                if (null != mOwner) {
                    mOwner.cityPicked(null);
                }

            }
        });
        mPicker = cityPicker;
        setSelected(mSelected);
        return view;
    }

    public void setSelected(LocationData place){
        if (null == mPicker){
            return;
        }
        mSelected = place;
        if (null != mSelected && mCitiesToShow.contains(mSelected)){
            mPicker.setSelection(mAdapter.getPosition(mSelected));
        }
    }


    public void setNewPlacesData(ArrayList<LocationData> newPlaces){
        if (null ==mPicker){
            Logger.w("Null picker reference, aborting");
            return;
        }
        LocationData prevSelected = (LocationData) mPicker.getSelectedItem();
        mCitiesToShow = new ArrayList<>();
        mCitiesToShow.addAll(newPlaces);
        mAdapter.clear();
        mAdapter.addAll(mCitiesToShow);
        if (mCitiesToShow.contains(prevSelected)){
            int i = mCitiesToShow.indexOf(prevSelected);
            mPicker.setSelection(i);
        }
    }


    private ArrayList<LocationData> mCitiesToShow;
    private LocationData mSelected;
    private Spinner mPicker;
    private LocationAdapter mAdapter;
    ICityPickerChannel mOwner;

}
