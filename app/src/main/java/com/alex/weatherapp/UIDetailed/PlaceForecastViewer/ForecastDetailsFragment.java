package com.alex.weatherapp.UIDetailed.PlaceForecastViewer;


import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.R;

import java.util.Calendar;

public class ForecastDetailsFragment extends Fragment implements View.OnClickListener {

    private static final String BUTTON_SHOW_ANOTHER_DAY = "BUTTON_SHOW_ANOTHER_DAY";
    private static final String BUTTON_SHOW_ANOTHER_PLACE = "BUTTON_SHOW_ANOTHER_PLACE";
    private static final String FORECAST_DAY = "FORECAST_DAY";
    private static final String FORECAST_NIGHT = "FORECAST_NIGHT";
    private static final String FORECAST_CONDITIONS = "FORECAST_CONDITIONS";
    private static final String PRECIPITATIONS_DAY = "PRECIPITATIONS_DAY";
    private static final String PRECIPITATIONS_NIGHT = "PRECIPITATIONS_NIGHT";
    private static final String FORECAST_LOW_TEMPERATURE = "TEMPERATURE_LOW";
    private static final String FORECAST_HIGH_TEMPERATURE = "TEMPERATURE_HIGH";
    private static final String FORECAST_AVERAGE_HUMUDITY = "TAVERAGE_HUMIDITY";
    private static final String FORECAST_YEAR = "FORECAST_YEAR";
    private static final String FORECAST_DAY_OF_YEAR = "FORECAST_DAY_OF_YEAR";

    public static ForecastDetailsFragment newInstance(Forecast.DayForecast dayForecast,
                                                      boolean showAnotherDayButtonAvailible,
                                                      boolean showAnotherPlaceButtonAvailible)  {
        ForecastDetailsFragment fragment = new ForecastDetailsFragment();

        Bundle args = new Bundle();
        args.putBoolean(BUTTON_SHOW_ANOTHER_DAY, showAnotherDayButtonAvailible);
        args.putBoolean(BUTTON_SHOW_ANOTHER_PLACE, showAnotherPlaceButtonAvailible);
        args.putString(FORECAST_DAY, dayForecast.dayTextForecast);
        args.putString(FORECAST_NIGHT, dayForecast.nightTextForecast);
        args.putString(FORECAST_CONDITIONS, dayForecast.conditions);
        args.putDouble(PRECIPITATIONS_DAY, dayForecast.precipDay);
        args.putDouble(PRECIPITATIONS_NIGHT, dayForecast.precipNight);
        args.putDouble(FORECAST_LOW_TEMPERATURE, dayForecast.tempLow);
        args.putDouble(FORECAST_HIGH_TEMPERATURE, dayForecast.tempHigh);
        args.putDouble(FORECAST_AVERAGE_HUMUDITY, dayForecast.averageHumidity);
        args.putInt(FORECAST_YEAR, dayForecast.year);
        args.putInt(FORECAST_DAY_OF_YEAR, dayForecast.dayOfYear);

        fragment.setArguments(args);
        return fragment;
    }

    public interface IDayFragmentHolderCallback {
        void onOtherDayButtonClicked();
        void onOtherPlaceButtonClicked();
    }

    public ForecastDetailsFragment() {
    }

    /**
     * try to cast the holding activity to holder callback
     * interface
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity holder = getActivity();
        if (holder == null) return;
        try {
            mFragmentHolder = (IDayFragmentHolderCallback) holder;
        }catch (ClassCastException e) {
            Log.d("Fragment failure", "Activity doesn't implement callback iface");
            return;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDayForecast = new Forecast.DayForecast();
        Bundle args = null;
        args = getArguments();
        mIsAnotherDayBtnActive = false;
        mIsAnotherPlaceBtnActive = false;
        if (args!= null) {
            parseForecastBundle(args, mDayForecast);
            mIsAnotherDayBtnActive = args.getBoolean(BUTTON_SHOW_ANOTHER_DAY);
            mIsAnotherPlaceBtnActive = args.getBoolean(BUTTON_SHOW_ANOTHER_PLACE);
        }
        //parseBundle(arg, mDayForecast);
    }

    private void parseForecastBundle(Bundle arg, Forecast.DayForecast forecast){
        forecast.dayTextForecast = arg.getString(FORECAST_DAY, "");
        forecast.nightTextForecast = arg.getString(FORECAST_NIGHT, "");
        forecast.conditions = arg.getString(FORECAST_CONDITIONS, "");
        forecast.precipDay = arg.getDouble(PRECIPITATIONS_DAY, 0);
        forecast.precipNight = arg.getDouble(PRECIPITATIONS_NIGHT, 0);
        forecast.tempLow = arg.getDouble(FORECAST_LOW_TEMPERATURE, 0);
        forecast.tempHigh = arg.getDouble(FORECAST_HIGH_TEMPERATURE, 0);
        forecast.averageHumidity = arg.getDouble(FORECAST_AVERAGE_HUMUDITY, 0);
        forecast.year = arg.getInt(FORECAST_YEAR, 2000);
        forecast.dayOfYear = arg.getInt(FORECAST_DAY_OF_YEAR, 0);
    }

    private void fillInFields(View view, Forecast.DayForecast forecast){
        /** Fill in the date field */
        final TextView dateField = (TextView)view.findViewById(R.id.idc_fd_date);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, forecast.year);
        calendar.set(Calendar.DAY_OF_YEAR, forecast.dayOfYear);
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
        int month =  calendar.get(Calendar.MONTH);
        Resources resources = getResources();
        String[] monthsNames = resources.getStringArray(R.array.months_names);
        StringBuilder sb = new StringBuilder();
        sb.append(monthDay);
        sb.append("-");
        sb.append(monthsNames[month]);
        sb.append("-");
        sb.append(forecast.year);
        String formatDate = sb.toString();
        dateField.setText(formatDate);
        /** figure out the current day of week, take its name from resources and set in the field */
        final TextView weekDayField = (TextView)view.findViewById(R.id.idc_fd_weekday);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 6;
        } else {
            weekDay -= 2;
        }
        String[] weekDays = resources.getStringArray(R.array.weekdays_names);
        weekDayField.setText(weekDays[weekDay]);
        /** Fill in conditions */
        final TextView conditionsField = (TextView)view.findViewById(R.id.idc_fd_conditions);
        conditionsField.setText(forecast.conditions);
        /** Fill in day and night forecasts */
        final TextView dayForecastField = (TextView)view.findViewById(R.id.idc_fd_day_forecast);
        dayForecastField.setText(forecast.dayTextForecast);
        final TextView nightForecastField = (TextView) view.findViewById(R.id.idc_fd_night_forecat);
        nightForecastField.setText(forecast.nightTextForecast);
        /** set temperature, humidity and precipitation, if there any */
        final TextView tempLowField = (TextView) view.findViewById(R.id.idc_fd_low_temperature);
        int tempLow = (int) forecast.tempLow;
        String tempLowText = String.valueOf(tempLow) + " " + getString(R.string.locale_fd_degrees);
        tempLowField.setText(tempLowText);
        int tempHigh = (int)forecast.tempHigh;
        String tempHighText = String.valueOf(tempHigh) + " " + getString(R.string.locale_fd_degrees);
        final TextView tempHighField = (TextView)view.findViewById(R.id.idc_fd_high_temperature);
        tempHighField.setText(tempHighText);

        int averageHumidity = (int) forecast.averageHumidity;
        String averageHumidityText = String.valueOf(averageHumidity) +
                " " + getString(R.string.locale_fd_percent);
        final TextView averHumidityField = (TextView)view.findViewById(R.id.idc_fd_aver_humidt);
        averHumidityField.setText(String.valueOf(averageHumidityText));

        /** Fill in precipitations fields. Make fields invisible if level is 0mm */
        double precipDay = forecast.precipDay;
        if (precipDay == 0) {
            LinearLayout precipDayFrame =
                    (LinearLayout)view.findViewById(R.id.idc_fd_frame_day_precip);
            precipDayFrame.setVisibility(View.GONE);
        } else {
            String precipDayText = String.valueOf(precipDay) + " " +
                    getString(R.string.locale_fd_millimeters);
            final TextView precipDayField = (TextView)view.findViewById(R.id.idc_fd_precip_day);
            precipDayField.setText(precipDayText);
        }
        double precipNight = forecast.precipNight;
        if (precipNight == 0) {
            LinearLayout precipNightFrame =
                    (LinearLayout)view.findViewById(R.id.idc_fd_frame_night_precip);
            precipNightFrame.setVisibility(View.GONE);
        } else {
            String precipNightText = String.valueOf(precipNight) + " " +
                    getString(R.string.locale_fd_millimeters);
            final TextView precipNightField = (TextView)view.findViewById(R.id.idc_fd_precip_night);
            precipNightField.setText(precipNightText);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.forecast_details, container ,false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fillInFields(view, mDayForecast);
        final Button btnOtherDay = (Button)view.findViewById(R.id.idc_fd_btn_show_others);
        if (!mIsAnotherDayBtnActive){
            btnOtherDay.setVisibility(View.GONE);
        }else {
            btnOtherDay.setOnClickListener(this);
        }
        final Button btnOtherPlace = (Button)view.findViewById(R.id.idc_fd_btn_other_place);
        if (!mIsAnotherPlaceBtnActive){
            btnOtherPlace.setVisibility(View.GONE);
        } else {
            btnOtherPlace.setOnClickListener(this);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.idc_fd_btn_show_others:
                mFragmentHolder.onOtherDayButtonClicked();
                break;
            case R.id.idc_fd_btn_other_place:
                mFragmentHolder.onOtherPlaceButtonClicked();
                break;
            default:
                break;
        }
    }

    private Forecast.DayForecast mDayForecast;
    private boolean mIsAnotherDayBtnActive;
    private boolean mIsAnotherPlaceBtnActive;

    private IDayFragmentHolderCallback mFragmentHolder = null;
}
