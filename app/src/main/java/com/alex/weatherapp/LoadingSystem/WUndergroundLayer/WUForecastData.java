package com.alex.weatherapp.LoadingSystem.WUndergroundLayer;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastData;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 06.09.2015.
 */
public class WUForecastData extends ForecastData {

    @SerializedName("response")
    public WUndergroundGeolookupData.Response mResponse = new WUndergroundGeolookupData.Response();

    public static class TextForecastRecord {
        @SerializedName("period")
        public int mRecordNo;
        public String icon = "";
        public String icon_url = "";
        public String title = "";
        @SerializedName("fcttext")
        public String forecastTextImperial;
        @SerializedName("fcttext_metric")
        public String forecastTextMetric;
        public int pop;
    };

    public static class TextForecast {
        String date = "";
        @SerializedName("forecastday")
        public List<TextForecastRecord> mForecastRecords = new ArrayList<>();

    };

    /**
     * Gets passed in period forecast (day or night)
     */
    public static class Date {
        public long epoch;
        @SerializedName("pretty")
        public String mDateInPrettyFormat;
        public int day;
        public int month;
        public int year;
        public int yday;
        public int hour;
        public int min;
        public int sec;
        public boolean isdst;
        public String monthname;
        public String monthname_short;
        public String weekday_short;
        public String weekday;
        public String ampm;
        public String tz_short;
        public String tz_long;
    };

    /**
     * stores temperature in celsius and farenheit degrees
     */
    public static class Temperature {
        public int fahrenheit;
        public int celsius;
    }

    /**
     * Stores info about precipitaions (inches / millimeters)
     */
    public static class Precipitation {
        @SerializedName("in")
        public double mInInches;
        @SerializedName("mm")
        public double mInMillimeters;
    };

    public static class Wind {
        public double mph;
        public double kph;
        public String dir;
        public double degrees;
    };

    public static class SimpleForecastRecord {
        // day now is a key in a map records are stored in
        //Date date = new Date();
        public int period;
        public Temperature high = new Temperature();
        public Temperature low = new Temperature();
        public String conditions;
        public String icon;
        public String icon_url;
        public String skyicon;
        public int pop;
        public Precipitation qpf_allday = new Precipitation();
        public Precipitation qpf_day = new Precipitation();
        public Precipitation qpf_night = new Precipitation();
        public Precipitation snow_allday = new Precipitation();
        public Precipitation snow_day = new Precipitation();
        public Precipitation snow_night = new Precipitation();
        @SerializedName("maxwind")
        public Wind mMaxWind = new Wind();
        @SerializedName("avewind")
        public Wind mAverageWind = new Wind();
        @SerializedName("avehumidity")
        public int mAverageHumidity;
        @SerializedName("maxhumidity")
        public int mMaximalHumidity;
        @SerializedName("minhumidity")
        public int mMinHumidity;

    };



    public static class SimpleForecast {
        @SerializedName("forecastday")
        public List<SimpleForecastRecord> mSimpleRecs = new ArrayList<>();
    }

    public static class Forecast {
        @SerializedName("txt_forecast")
        public TextForecast mShortTextForecast = new TextForecast();
        @SerializedName("simpleforecast")
        public SimpleForecast mSimpleForecast = new SimpleForecast();
    }




    @SerializedName("forecast")
    public Forecast mForecast = new Forecast();

}
