package com.alex.weatherapp.LoadingSystem.WUndergroundLayer;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupData;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 05.09.2015.
 */

/*
    Geolookup response data from WUnderground service, variables has the same names as
    JSON fields does and self-explanatory, see http://www.wunderground.com/weather/api/d/docs?d=data/geolookup&MR=1 for
    more details
 */

public class WUndergroundGeolookupData extends GeolookupData {

    public static class Features {
        public int geolookup;
    }

    public static class Response {
        public double version;
        public String termsofService;
        Features features = new Features();
    };

    public Response response = new Response();


    public static class WeatherStationData    {
        public String city;
        public String state;
        public String country;
        public String icao;
        public double lat;
        public double lon;
    };

    public static class Airport {
        @SerializedName("station")
        public List<WeatherStationData> mStations = new ArrayList<>();
    };

    public static class PersonalWeatherStationData {
        public String neighborhood;
        public String city;
        public String state;
        public String country;
        public String id;
        public double lat;
        public double lon;
        public double distance_km;
        public double distance_mi;
    };

    public static class PWS {
        @SerializedName("station")
        List<PersonalWeatherStationData> mPWSs = new ArrayList<>();
    };

    public static class NearbyWeatherStations {
        public Airport airport = new Airport();
        public PWS pws = new PWS();
    }

    public static class Location {
        public String type;
        public String country;
        public String country_iso3166;
        public String country_name;
        public String state;
        public String city;
        public String tz_short;
        public String tz_long;
        public double lat;
        public double lon;
        public int zip;
        public int magic;
        public int wmo;
        public String l;
        public String requesturl;
        public String wuiurl;

        public NearbyWeatherStations nearby_weather_stations = new NearbyWeatherStations();
    };

    public Location location = new Location();






    public static class Neighborhood {
        public String neighborhood;
        public String  city;
        public String state;
        public String country;
        public String id;
        public double lat;
        public double lon;
        public double distance_km;
        public double distance_mi;
    }


}
