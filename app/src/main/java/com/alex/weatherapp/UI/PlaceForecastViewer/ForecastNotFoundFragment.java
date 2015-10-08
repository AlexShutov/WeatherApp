package com.alex.weatherapp.UI.PlaceForecastViewer;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alex.weatherapp.R;

public class ForecastNotFoundFragment extends Fragment {
    public ForecastNotFoundFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.forecas_details_empty, container, false);
        return v;
    }


}
