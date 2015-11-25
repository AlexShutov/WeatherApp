package com.alex.weatherapp.UIv2.ForecastViewer;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.R;

import org.w3c.dom.Text;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class DayForecastSimpleViewer extends Fragment {
    private static final String FORECAST_IS_BIG_STYLE = "FORECAST_IS_BIG_STYLE";

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

    private class ViewTag{
        TextView tempField;
        TextView dateField;
        TextView shortForecast;
    }



    public static DayForecastSimpleViewer newInstance(Forecast.DayForecast dayForecast,
                                                      boolean isBigStyle){
        Bundle args = new Bundle();
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
        args.putBoolean(FORECAST_IS_BIG_STYLE, isBigStyle);
        DayForecastSimpleViewer newInstance = new DayForecastSimpleViewer();
        newInstance.setArguments(args);
        return newInstance;
    }

    public static void showDayForecast(Forecast.DayForecast forecast,
                                       boolean isBigStyle,
                                       int containerID,
                                       Activity activity){
        FragmentManager fm = activity.getFragmentManager();
        DayForecastSimpleViewer smallForecast =
                DayForecastSimpleViewer.newInstance(forecast, isBigStyle);
        fm.beginTransaction().replace(containerID, smallForecast).commit();
    }

    /**
     * Method is taken from old ui version's ForecastDetailsFragment
     * @param arg
     * @param forecast
     */
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


    public DayForecastSimpleViewer() {
        mIsBigStyle = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDayForecast = new Forecast.DayForecast();
        Bundle args = null;
        args = getArguments();
        if (null != args){
            parseForecastBundle(args, mDayForecast);
            mIsBigStyle = args.getBoolean(FORECAST_IS_BIG_STYLE, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        ViewTag tag = new ViewTag();
        if (mIsBigStyle){
            view = inflater.inflate(R.layout.day_forecast_big, container, false);
            tag.dateField = (TextView) view.findViewById(R.id.idc_dfb_date);
            tag.tempField = (TextView) view.findViewById(R.id.idc_dfb_temp);
            tag.shortForecast = (TextView) view.findViewById(R.id.idc_dfb_forecast_short);
        }else {
            view = inflater.inflate(R.layout.day_forecast_small, container, false);
            tag.dateField = (TextView)view.findViewById(R.id.idc_dfs_date);
            tag.tempField = (TextView)view.findViewById(R.id.idc_dfs_temp);
            tag.shortForecast = (TextView) view.findViewById(R.id.idc_dfs_short_forecast);
        }
        view.setTag(tag);
        fillInFields(mDayForecast, view);
        return view;
    }

    public void fillInFields(Forecast.DayForecast forecast, View v){
        if (forecast != mDayForecast){
            mDayForecast = forecast;
        }
        if (null == v){
            return;
        }
        ViewTag tag = (ViewTag) v.getTag();
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
        tag.dateField.setText(formatDate);
        tag.shortForecast.setText(forecast.conditions);

        int tempLow = (int) forecast.tempLow;
        String lowSign = (tempLow < 0)? "-" : "+";
        if (tempLow == 0){
            lowSign = "";
        }
        tempLow = Math.abs(tempLow);
        int tempHigh = (int)forecast.tempHigh;
        String highSign = (tempHigh < 0) ? "-" : "+";
        if (tempHigh == 0){
            highSign = "";
        }
        tempHigh = Math.abs(tempHigh);
        String temperature = lowSign + String.valueOf(tempLow) + ".." +
                highSign + String.valueOf(tempHigh) + "C";
        tag.tempField.setText(temperature);
    }


    private boolean mIsBigStyle;
    private Forecast.DayForecast mDayForecast;
}
