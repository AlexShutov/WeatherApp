package com.alex.weatherapp.LoadingSystem.ForecastRequest;

import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUForecastData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex on 07.09.2015.
 */

/**
 * struct, carrying day forecast
 */

public class Forecast extends  ForecastData {

        //public static final String sDayFormat = "yyyy MMM dd";
        public static final SimpleDateFormat sSdf = new SimpleDateFormat("yyyy MMM dd");

        public static int testCalendarClass(Date date) {
            if (date == null) {
                return  0;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");

            // my birthday
            calendar.set(Calendar.YEAR, 2015);
            calendar.set(Calendar.DAY_OF_YEAR, 160);
            String calStr = sdf.format(calendar.getTime());

            int year       = calendar.get(Calendar.YEAR);
            int month      = calendar.get(Calendar.MONTH); // Jan = 0, dec = 11
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
            int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
            int weekOfMonth= calendar.get(Calendar.WEEK_OF_MONTH);
            Date t = calendar.getTime();

            Calendar weekBegin = Calendar.getInstance();
            calStr = sdf.format(calendar.getTime());

            weekBegin.setTime(t);
            weekBegin.set(Calendar.DAY_OF_WEEK, 2);
            weekBegin.set(Calendar.WEEK_OF_YEAR, weekOfYear);
            weekBegin.set(Calendar.WEEK_OF_MONTH, weekOfMonth);

            String wBegStr = sdf.format(weekBegin.getTime());

            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (weekDay == 1) weekDay = 7;
            else
                weekDay--;
            return  weekDay;
        }

    /**
     * Sometimes API doesn't know anything about precipitations and returns null. Instead of writing
     * custom GSon adapter, it's easier to check the data here. Actually, i wrote TypeAdapter for
     * Double, but it doesn't work, so check values in here
     * @param precip
     */
    private static void verifyPrecipitations(WUForecastData.Precipitation precip) {
        if (precip == null) {
            precip = new WUForecastData.Precipitation();
        }
       // if (precip.mInMillimeters == null) precip.mInMillimeters = new Double(0);
       // if (precip.mInInches == null) precip.mInInches = new Double(0);
    }

    public static Forecast convertForecastFromWUndergroundData( WUForecastData forecastData)
    {
        Forecast result = new Forecast();
        if (forecastData == null)   // return empty result
            return  result;
        //SimpleDateFormat sdf = new SimpleDateFormat(sDayFormat);

        DayForecast currDay = null;
        Calendar calendar = Calendar.getInstance();
        /* forecasts in UWnderground response are started from today, so
          * we need to create date for an each day, the simplest way to do that- increase
          * Calendar.DAY_OF_YEAR every loop iteration */
        int dayOfYear4FirstRec = calendar.get(Calendar.DAY_OF_YEAR);

        WUForecastData.SimpleForecastRecord currDayWU = null;
        WUForecastData.SimpleForecast simpleForecast = forecastData.mForecast.mSimpleForecast;

        int nRecs = simpleForecast.mSimpleRecs.size();
        if (nRecs == 0)
            return new Forecast();
        for (int i = 0; i < nRecs; ++i) {
            currDay = new DayForecast();
            currDayWU = simpleForecast.mSimpleRecs.get(i);

            /* Calculate a date for current forecst record and fill in date fields */
            calendar.set(Calendar.DAY_OF_YEAR, dayOfYear4FirstRec + i);
            currDay.year = calendar.get(Calendar.YEAR);
            currDay.dayOfYear  = calendar.get(Calendar.DAY_OF_YEAR);
            /* fill in other fields but text forecasts and icons */
            currDay.isDummy = false;
            currDay.conditions = currDayWU.conditions;
            currDay.tempLow = currDayWU.low.celsius;
            currDay.tempHigh = currDayWU.high.celsius;
            //currDay.maxWind = currDayWU.mMaxWind.kph;
           // currDay.averageWind = currDayWU.mAverageWind.kph;
            verifyPrecipitations(currDayWU.qpf_day);
           // currDay.precipDay = currDayWU.qpf_day.mInMillimeters;
            verifyPrecipitations(currDayWU.qpf_night);
           // currDay.precipNight = currDayWU.qpf_night.mInMillimeters;
            currDay.averageHumidity = currDayWU.mAverageHumidity;
            result.mDayForecasts.add(currDay);
        }
        /* Now consider short text descriptions. Each description contains weekday name + day time
         * in form 'Monday' or 'Monday Night' and also year and day of the year plus short
         * text forecast and url to weather icon */

        List<WUForecastData.TextForecastRecord> textDayForecasts =
                forecastData.mForecast.mShortTextForecast.mForecastRecords;

        int dayIndex = 0;
        int textForcIndex = 0;

        WUForecastData.TextForecastRecord currTextForc =
                textDayForecasts.get(0);
        currDay = result.mDayForecasts.get(0);

        /* When forecast is acquird in the evening, first text forecast is for the current night */
        String title = currTextForc.title;
        String splittedTitle[] = currTextForc.title.split(" ");
        boolean firstRecordisNight =
                splittedTitle.length == 2 && splittedTitle[1].toLowerCase().equals("night");

        if (firstRecordisNight) {
            currDay.dayTextForecast = "";
            currDay.nightTextForecast = currTextForc.forecastTextMetric;
            currDay.nightWeatherIcon = currTextForc.icon_url;
            textForcIndex++;
            dayIndex++;
        }
        int nDays = result.mDayForecasts.size();
        int nTextForc = textDayForecasts.size();
        while (dayIndex < nDays && textForcIndex < nTextForc) {
            currTextForc = textDayForecasts.get(textForcIndex);
            currDay = result.mDayForecasts.get(dayIndex);
            /* copy day forecast */
            currDay.dayTextForecast = currTextForc.forecastTextMetric;
            currDay.dayWeatherIcon = currTextForc.icon_url;
            /* copy night forecast */
            currTextForc = textDayForecasts.get(++textForcIndex);
            currDay.nightTextForecast = currTextForc.forecastTextMetric;
            currDay.nightWeatherIcon = currTextForc.icon_url;
            // increment counters
            textForcIndex++;
            dayIndex++;
        }
        return result;
    }

    public Forecast() {
        mDayForecasts = new ArrayList<>();
    }

    public static class DayForecast implements Comparable<DayForecast> {

        @Override
        public int hashCode() {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.DAY_OF_YEAR, dayOfYear);
            return sSdf.format(c.getTime()).hashCode();
        }

        /* *
        Sorts records by date, we only need to do that during saving forecast into
        local storage
         */
        @Override
        public int compareTo(DayForecast another) {
            if (year < another.year) return  -1;
            if (year > another.year) return  1;
            if (dayOfYear < another.dayOfYear) return  -1;
            if (dayOfYear > another.dayOfYear) return  1;
            return  0;
        }

        /* Use Calendar to extract the date  */
        public int year;
        public int dayOfYear;

        public String dayTextForecast;
        public String dayWeatherIcon;
        public String nightTextForecast;
        public String nightWeatherIcon;

        public String conditions;
        public double tempHigh;
        public double tempLow;
        public double maxWind;
        public double averageWind;
        public double precipDay;
        public double precipNight;
        public double averageHumidity;

        public boolean isDummy;


    };


    /* Key is the string  */
    public List<DayForecast> mDayForecasts;

};
