package com.alex.weatherapp.UI.PlaceForecastViewer;


import android.app.ListFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DaysDetailsFragment extends ListFragment {

    private static final String TAG_YEAR_ARRAY = "TAG_YEAR_ARRAY";
    private static final String TAG_DAY_OF_YEAR_ARRAY = "TAG_DAY_OF_YEAR_ARRAY";


    public interface IDaysDetailsFragmentHolder {
        /**
         * when this method is called fragment the holder (activity) already received the data,
         * and activity's method will be redirected to  according strategy
         * Subscription to the holder occurs before ListView is initialzed.
         */
        void onDaySelected(int position);
    }

    public static DaysDetailsFragment newInstance(Forecast forecast){
        Bundle args = new Bundle();
        ArrayList<String> yearFields = new ArrayList<>();
        ArrayList<String> dayOfYearFields = new ArrayList<>();
        for (Forecast.DayForecast f : forecast.mDayForecasts){
            yearFields.add(String.valueOf(f.year));
            dayOfYearFields.add(String.valueOf(f.dayOfYear));
        }
        if (yearFields.size() != 0) {
            args.putStringArrayList(TAG_YEAR_ARRAY, yearFields);
            args.putStringArrayList(TAG_DAY_OF_YEAR_ARRAY, dayOfYearFields);
        }

        DaysDetailsFragment fragment = new DaysDetailsFragment();
        fragment.setArguments(args);
        return  fragment;
    }

    public DaysDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();

        ArrayList<String> yearFields = null;
        ArrayList<String> dayOfYearFields = null;

        mForecast = new Forecast();
        yearFields = arg.getStringArrayList(TAG_YEAR_ARRAY);
        dayOfYearFields = arg.getStringArrayList(TAG_DAY_OF_YEAR_ARRAY);
        if (yearFields == null || dayOfYearFields == null || yearFields.size() == 0){
            return;
        }
        int n = yearFields.size();
        for (int i = 0; i < n; ++i){
            int year = Integer.valueOf(yearFields.get(i));
            int dayInYear = Integer.valueOf(dayOfYearFields.get(i));
            Forecast.DayForecast df = new Forecast.DayForecast();
            df.year = year;
            df.dayOfYear = dayInYear;
            mForecast.mDayForecasts.add(df);
        }
    }

    /**
     * Cast holding activity and save it as a callback; create and set adapter
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHolder = null;
        try {
            mHolder = (IDaysDetailsFragmentHolder) getActivity();
        }catch (ClassCastException e){
            Log.d("DaysDetailsFragment", "Activity doesn't iimplement callback interface,");
        }
        DaysDetailsAdapter adapter = new DaysDetailsAdapter(getActivity(), mForecast);
        setListAdapter(adapter);
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mHolder != null){
            mHolder.onDaySelected(position);
        }
    }
    @Override
    public int getSelectedItemPosition() {
        return super.getSelectedItemPosition();
    }
    @Override
    public void setSelection(int position) {
        super.setSelection(position);
    }

    IDaysDetailsFragmentHolder mHolder;
    Forecast mForecast;
}
