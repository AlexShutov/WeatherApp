package com.alex.weatherapp.UI.PlacesViewer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 06.10.2015.
 */
public class PlaceNameAdapter extends BaseAdapter {

    private int mColorNameOnly;
    private int mColorProcessedLocally;
    private int mColorProcessedOnline;

    /** For a view holder pattern */
    private static class ViewTag {
        TextView placeNameField;
        TextView latitudeField;
        TextView longitudeField;
    }

    public PlaceNameAdapter(Context context, List<LocationData> places, List<PlaceUpdateState> states){
        updateData(places, states);
        Resources res = context.getResources();
        mColorNameOnly = res.getColor(R.color.color_pn_name_only);
        mColorProcessedLocally = res.getColor(R.color.color_pn_processed_locally);
        mColorProcessedOnline = res.getColor(R.color.color_pn_processed_online);
    }

    public void updateData(List<LocationData> places, List<PlaceUpdateState> states){
        if (places == null || states == null ||
                places.size() != states.size()){
            places = new ArrayList<>();
            states = new ArrayList<>();
        }
        List<RowData> data = new ArrayList<>();
        for (int i = 0; i < places.size(); ++i){
            RowData currRow = new RowData(places.get(i), states.get(i));
            data.add(currRow);
        }
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewTag tag = null;
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) parent.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.place_name, parent, false);
            tag = new ViewTag();
            tag.placeNameField = (TextView)view.findViewById(R.id.idc_pn_place_name);
            tag.latitudeField = (TextView)view.findViewById(R.id.idc_pn_latitude);
            tag.longitudeField = (TextView)view.findViewById(R.id.idc_pn_longitude);
            view.setTag(tag);
        } else {
            tag = (ViewTag) convertView.getTag();
        }
        RowData row = (RowData) getItem(position);
        LocationData place = row.getPlace();
        /** Set colors of text and background color depending on state of request */
        tag.placeNameField.setText(place.getmPlaceName());
        tag.latitudeField.setText(String.valueOf(place.getLat()));
        tag.longitudeField.setText(String.valueOf(place.getLon()));
        tag.placeNameField.setTextColor(Color.BLACK);
        tag.latitudeField.setTextColor(Color.BLACK);
        tag.longitudeField.setTextColor(Color.BLACK);
        switch (row.getState()){
            case NameOnly:
                view.setBackgroundColor(mColorNameOnly);
                break;
            case ProcessedCache:
                view.setBackgroundColor(mColorProcessedLocally);
                break;
            case ProcessedOnline:
                view.setBackgroundColor(mColorProcessedOnline);
                break;
            default:
                break;
        }
        return view;
    }

    private List<RowData> mData;
}
