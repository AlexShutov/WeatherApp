package com.alex.weatherapp.UIDetailed.PlaceForecastViewer;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Alex on 05.10.2015.
 */
public class DaysDetailsAdapter extends BaseAdapter {
    private static class DayTag {
        public TextView mDate;
        public TextView mWeekDayName;
    }

    public DaysDetailsAdapter(Context context, Forecast forecast){
        mContext = context;
        if (forecast == null) forecast = new Forecast();
        mForecastToShow = forecast;
        mInflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return mForecastToShow.mDayForecasts.get(position);
    }

    @Override
    public int getCount() {
        return mForecastToShow.mDayForecasts.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = mInflter.inflate(R.layout.day_details, parent, false);
            DayTag tag = new DayTag();
            tag.mDate = (TextView)view.findViewById(R.id.idc_dd_date);
            tag.mWeekDayName = (TextView)view.findViewById(R.id.idc_dd_weekday);
            view.setTag(tag);
        }
        DayTag holder = (DayTag) view.getTag();
        Forecast.DayForecast dayForecast = mForecastToShow.mDayForecasts.get(position);
        /** This conde fragment is the same, as in ForecastDetailsFragment */
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, dayForecast.year);
        calendar.set(Calendar.DAY_OF_YEAR, dayForecast.dayOfYear);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMMMMM-yyyy");
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
        int month =  calendar.get(Calendar.MONTH);
        Resources resources = mContext.getResources();
        String[] monthsNames = resources.getStringArray(R.array.months_names);
        StringBuilder sb = new StringBuilder();
        sb.append(monthDay);
        sb.append("-");
        sb.append(monthsNames[month]);
        sb.append("-");
        sb.append(dayForecast.year);
        String formatDate = sb.toString();
        holder.mDate.setText(formatDate);
        /** figure out the current day of week, take its name from resources and set in the field */
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (weekDay == 1) {
            weekDay = 6;
        } else {
            weekDay -= 2;
        }

        String[] weekDays = resources.getStringArray(R.array.weekdays_names);
        holder.mWeekDayName.setText(weekDays[weekDay]);
        return view;
    }

    private Context  mContext;
    private Forecast mForecastToShow;
    LayoutInflater mInflter;
}
