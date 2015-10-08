package com.alex.weatherapp.UI.PlacesViewer;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alex.weatherapp.R;

public class NoPlacesFoundFragment extends Fragment implements View.OnClickListener {


    public NoPlacesFoundFragment() {
        // Required empty public constructor
        mClickListener = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mClickListener = (IHolderInterface) activity;
        }catch (ClassCastException ce){
            throw new IllegalArgumentException("Activity must implement "+
            "IForecastViewer.IPlaceSelectedCallback");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.place_name_empty, container, false);
        view.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        mClickListener.onEmptyViewClicked();
    }

    private IHolderInterface mClickListener;
}
