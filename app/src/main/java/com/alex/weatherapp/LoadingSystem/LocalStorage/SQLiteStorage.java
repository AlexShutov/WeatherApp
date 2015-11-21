package com.alex.weatherapp.LoadingSystem.LocalStorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupData;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex on 08.09.2015.
 */

public class SQLiteStorage {

    /* table name and names for the fields of DayForecast */
    public static class ForecastTable {
        /* unique ID of forecast record */
        public static final String ID = "_id";
        /* DB table name */
        public  static final String TABLE_NAME = "forecast_records";
        /** DB fields names according to DayForecast fields.
         * Day in db is inserted and referenced by its string name (yyyy MMM dd),
         * even though selection may be done by year and year day fields
         */
        public static final String DATE_STRING_FORMAT = "date_string_format";
        public static final String PLACE_LATITUDE = "place_geo_latitude";
        public static final String PLACE_LONGITUDE = "place_longitude";
        public static final String YEAR = "year";
        public static final String DAY_OF_YEAR = "day_of_year";
        public static final String DAY_TEXT_FORECAST = "day_text_forecast";
        public static final String DAY_WEATHER_ICON_URL = "day_weather_icon_url";
        public static final String NIGHT_TEXT_FORECAST = "night_text_forecast";
        public static final String NIGHT_WEATHER_ICON_URL = "night_weather_icon_url";
        public static final String CONDITIONS = "conditions";
        public static final String TEMP_HIGH = "temperature_high";
        public static final String TEMP_LOW = "temperature_low";
        public static final String MAX_WIND_SPEED = "maximal_wind_speed";
        public static final String AVER_WIND_SPEED = "aerage_wind_speed";
        public static final String PRECIPIATIONS_DAY = "precip_day";
        public static final String PRECIPITATIONS_NIGHT = "precip_night";
        public static final String AVER_HUMIDITY = "average_humidity";
        public static final String IS_DUMMY = "is_dummy";
    };

    public static class PlacesNamesTable {
        public static final String ID = "_id";
        public static final String TABLE_NAME = "places_names";
        public static final String PLACE_LATITUDE = ForecastTable.PLACE_LATITUDE;
        public static final String PLACE_LONGITUDE = ForecastTable.PLACE_LONGITUDE;
        public static final String PLACE_NAME = "PLACE_NAME";
    };

    public static class ForecastSchemaHelper extends SQLiteOpenHelper implements ILocalStorageRequests  {

        private static final String DATABASE_NAME = "forecast_data.db";
        private static final int DATABASE_VERSION = 1;

        public ForecastSchemaHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        private void createForecastTable(SQLiteDatabase db) {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE " + ForecastTable.TABLE_NAME + " (" +
                    ForecastTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
            /* place coordinates */
            sb.append(ForecastTable.PLACE_LATITUDE + " TEXT, ");
            sb.append(ForecastTable.PLACE_LONGITUDE + " TEXT, ");
            /* Date string format for easy search  */
            sb.append(ForecastTable.DATE_STRING_FORMAT + " TEXT, ");
            sb.append(ForecastTable.YEAR + " INTEGER, ");
            sb.append(ForecastTable.DAY_OF_YEAR + " INTEGER, ");
            sb.append(ForecastTable.DAY_TEXT_FORECAST + " TEXT, ");
            sb.append(ForecastTable.DAY_WEATHER_ICON_URL + " TEXT, ");
            sb.append(ForecastTable.NIGHT_TEXT_FORECAST + " TEXT, ");
            sb.append(ForecastTable.NIGHT_WEATHER_ICON_URL + " TEXT, ");
            sb.append(ForecastTable.CONDITIONS + " TEXT, ");
            sb.append(ForecastTable.TEMP_HIGH + " REAL, ");
            sb.append(ForecastTable.TEMP_LOW + " REAL, ");
            sb.append(ForecastTable.MAX_WIND_SPEED + " REAL, ");
            sb.append(ForecastTable.AVER_WIND_SPEED + " REAL, ");
            sb.append(ForecastTable.PRECIPIATIONS_DAY + " REAL, ");
            sb.append(ForecastTable.PRECIPITATIONS_NIGHT + " REAL, ");
            sb.append(ForecastTable.AVER_HUMIDITY + " REAL, ");
            sb.append(ForecastTable.IS_DUMMY + " INTEGER");
            sb.append(");");

            String creationRequest = sb.toString();
            db.execSQL(creationRequest);
        }

        private void createPlacesTable(SQLiteDatabase db) {
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE " + PlacesNamesTable.TABLE_NAME + " (" +
                    PlacesNamesTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ");
            sb.append(PlacesNamesTable.PLACE_NAME + " TEXT, ");
            sb.append(PlacesNamesTable.PLACE_LATITUDE + " TEXT, ");
            sb.append(PlacesNamesTable.PLACE_LONGITUDE + " Text");
            sb.append(");");

            String creqtionRequest = sb.toString();
            db.execSQL(creqtionRequest);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            createForecastTable(db);
            createPlacesTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("LOG_TAG", "Upgrading database from version "
                    + oldVersion + " to " + newVersion +
                    " ,which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + ForecastTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + PlacesNamesTable.TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void dropForecastTable() {
            /* the same as OnUpdate */
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + ForecastTable.TABLE_NAME);
            createForecastTable(db);
        }

        @Override
        public void dropPlacesTable() {
            /* the same as OnUpdate */
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + PlacesNamesTable.TABLE_NAME);
            createPlacesTable(db);
        }

        @Override
        public boolean addNewPlace(LocationData place) {
            if (place == null) return  false;
            /** remove place if it's already exist in db */
            deletePlaceByCoord(place);
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(PlacesNamesTable.PLACE_NAME, place.getmPlaceName());
            cv.put(PlacesNamesTable.PLACE_LATITUDE, place.getLat());
            cv.put(PlacesNamesTable.PLACE_LONGITUDE, place.getLon());
            long insertResult = 0;
            try {
                insertResult = db.insertOrThrow(PlacesNamesTable.TABLE_NAME, null, cv);
            }catch (Exception e) {
                String msg = e.getMessage();
                return  false;
            }
            return true;
        }

        @Override
        public void getOnePlaceByCoordinates(LocationData place) {
            SQLiteDatabase db = getWritableDatabase();
            String selectionClause = PlacesNamesTable.PLACE_LATITUDE + "= ? AND " +
                    PlacesNamesTable.PLACE_LONGITUDE + " = ? ";
            String[] selectionArgs = new String[]{String.valueOf(place.getLat()),
                String.valueOf(place.getLon())};
            Cursor c = db.query(PlacesNamesTable.TABLE_NAME, null, selectionClause, selectionArgs,
                    null, null, null);
            c.moveToFirst();
            String name = "";
            try {
                name = c.getString(c.getColumnIndex(PlacesNamesTable.PLACE_NAME));
            }catch (Exception e) {
                name = "Unknown";
            }
            place.setmPlaceName(name);
        }

        @Override
        public LocationResponse getAllPlaces() {
            List<LocationData> result = new ArrayList<>();
            SQLiteDatabase db = getWritableDatabase();
            /* query all unique coordinates, save them in coords, and for every coordinate query
               all day forecast */
            String[] cols = new String[]{ PlacesNamesTable.PLACE_LATITUDE,
                    PlacesNamesTable.PLACE_LONGITUDE};
            Cursor c = db.query(true, PlacesNamesTable.TABLE_NAME, null, null, null, null,
                    null, null, null);
            while (c.moveToNext()){
                String placeName = c.getString(c.getColumnIndex(PlacesNamesTable.PLACE_NAME));
                double lat = c.getDouble(c.getColumnIndex(PlacesNamesTable.PLACE_LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(PlacesNamesTable.PLACE_LONGITUDE));
                LocationData loc = new LocationData(lat, lon, placeName);
                result.add(loc);
            }
            return new LocationResponse(result);
        }

        @Override
        public void deletePlaceByCoord(LocationData place) {
            SQLiteDatabase db = getWritableDatabase();
            String whereClause = PlacesNamesTable.PLACE_LATITUDE + " = ? AND " +
                    PlacesNamesTable.PLACE_LONGITUDE + " = ? ";
            String[] whereArgs = new String[]{ String.valueOf(place.getLat()),
                String.valueOf(place.getLon())};
            int N = db.delete(PlacesNamesTable.TABLE_NAME, whereClause, whereArgs);
        }

        @Override
        public boolean addNewForecast(GeolookupData coord, Forecast forecast)
        {
            if (forecast == null) return false;
            /* Create and fill in a ContentValues object  */
                ContentValues cv = null;
                SQLiteDatabase db = getWritableDatabase();
                Calendar c = Calendar.getInstance();
                long insertResult = 0;
                for (Forecast.DayForecast df : forecast.mDayForecasts) {
                    cv = new ContentValues();
                /* put coordinates and string date */
                    cv.put(ForecastTable.PLACE_LATITUDE, coord.getLat());
                    cv.put(ForecastTable.PLACE_LONGITUDE, coord.getLon());
                    String date = Forecast.sSdf.format(c.getTime());
                    cv.put(ForecastTable.DATE_STRING_FORMAT, date);
                    /* put the rest of a fields */
                    cv.put(ForecastTable.YEAR, df.year);
                    cv.put(ForecastTable.DAY_OF_YEAR, df.dayOfYear);
                    cv.put(ForecastTable.DAY_TEXT_FORECAST, df.dayTextForecast);
                    cv.put(ForecastTable.DAY_WEATHER_ICON_URL, df.dayWeatherIcon);
                    cv.put(ForecastTable.NIGHT_TEXT_FORECAST, df.nightTextForecast);
                    cv.put(ForecastTable.NIGHT_WEATHER_ICON_URL, df.nightWeatherIcon);
                    cv.put(ForecastTable.CONDITIONS, df.conditions);
                    cv.put(ForecastTable.TEMP_HIGH, df.tempHigh);
                    cv.put(ForecastTable.TEMP_LOW, df.tempLow);
                    cv.put(ForecastTable.MAX_WIND_SPEED, df.maxWind);
                    cv.put(ForecastTable.AVER_WIND_SPEED, df.averageWind);
                    cv.put(ForecastTable.PRECIPIATIONS_DAY, df.precipDay);
                    cv.put(ForecastTable.PRECIPITATIONS_NIGHT, df.precipNight);
                    cv.put(ForecastTable.AVER_HUMIDITY, df.averageHumidity);
                    int isDummy = (df.isDummy) ? 1 : 0;
                    cv.put(ForecastTable.IS_DUMMY, isDummy);
                    try {
                        insertResult = db.insertOrThrow(ForecastTable.TABLE_NAME, null, cv);
                        int a = 213;
                    }catch (Exception e) {
                        String msg = e.getMessage();
                        return  false;
                    }
                }
            return true;
        }

        @Override
        public Forecast getRecordsByCoordinates(GeolookupData coord) {

            SQLiteDatabase db = getWritableDatabase();
            String selectionClause = ForecastTable.PLACE_LATITUDE + "= ? AND " +
                    ForecastTable.PLACE_LONGITUDE + " = ? ";
            String[] selectionArgs = new String[]{ String.valueOf(coord.getLat()),
                    String.valueOf(coord.getLon())};
            Cursor c = db.query(ForecastTable.TABLE_NAME, null, selectionClause, selectionArgs,
                    null, null, null);
            List<Forecast.DayForecast> result = new ArrayList<>();
            Forecast.DayForecast currData = null;
            while (c.moveToNext()) {
                currData = new Forecast.DayForecast();
                extractDayForecastFromCursor(c, currData);
                result.add(currData);
            }
            int size = result.size();
            Forecast f = new Forecast();
            f.mDayForecasts = result;
            return f;
        }

        private void extractDayForecastFromCursor( Cursor c, Forecast.DayForecast currData) {
            currData.year      = c.getInt(c.getColumnIndex(ForecastTable.YEAR));
            currData.dayOfYear = c.getInt(c.getColumnIndex(ForecastTable.DAY_OF_YEAR));
            currData.dayTextForecast =
                    c.getString(c.getColumnIndex(ForecastTable.DAY_TEXT_FORECAST));
            currData.dayWeatherIcon =
                    c.getString(c.getColumnIndex(ForecastTable.DAY_WEATHER_ICON_URL));
            currData.nightTextForecast =
                    c.getString(c.getColumnIndex(ForecastTable.NIGHT_TEXT_FORECAST));
            currData.nightWeatherIcon =
                    c.getString(c.getColumnIndex(ForecastTable.NIGHT_WEATHER_ICON_URL));
            currData.conditions = c.getString(c.getColumnIndex(ForecastTable.CONDITIONS));
            currData.tempHigh = c.getDouble(c.getColumnIndex(ForecastTable.TEMP_HIGH));
            currData.tempLow = c.getDouble(c.getColumnIndex(ForecastTable.TEMP_LOW));
            currData.maxWind = c.getDouble(c.getColumnIndex(ForecastTable.MAX_WIND_SPEED));
            currData.averageWind = c.getDouble(c.getColumnIndex(ForecastTable.AVER_WIND_SPEED));
            currData.precipDay = c.getDouble(c.getColumnIndex(ForecastTable.PRECIPIATIONS_DAY));
            currData.precipNight = c.getDouble(c.getColumnIndex(ForecastTable.PRECIPITATIONS_NIGHT));
            currData.averageHumidity = c.getDouble(c.getColumnIndex(ForecastTable.AVER_HUMIDITY));
            currData.isDummy = (c.getInt(c.getColumnIndex(ForecastTable.IS_DUMMY)) != 0) ? true : false;
        }

        /**
         * @return All distinc coordinates, db has refords for
         */
        @Override
        public List<GeolookupData> getDistinctCoordinates() {
            List<GeolookupData> coords = new ArrayList<>();
            SQLiteDatabase db = getWritableDatabase();
            /* query all unique coordinates, save them in coords, and for every coordinate query
               all day forecast */
            String[] cols = new String[]{ ForecastTable.PLACE_LATITUDE, ForecastTable.PLACE_LONGITUDE};
            Cursor c = db.query(true, ForecastTable.TABLE_NAME, cols, null, null, null, null, null, null);
            while (c.moveToNext()) {
                double lat = c.getDouble(c.getColumnIndex(ForecastTable.PLACE_LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(ForecastTable.PLACE_LONGITUDE));
                coords.add(new GeolookupData(lat, lon));
            }
            return  coords;
        }

        /** query all unique coordinates, save them in 'coords', and for every coordinate query
         * all day forecast. Day forecasts then is united to Forecast and gets stored in 'forecasts'
         */
        @Override
        public Pair<List<Forecast>,List<GeolookupData> > getAllRecordsForAllLocations() {
            List<Forecast> forecasts = new ArrayList<>();
            List<GeolookupData> coords = getDistinctCoordinates();
            Forecast currCoordFc = null;
            for (GeolookupData gd : coords) {
                currCoordFc = getRecordsByCoordinates(gd);
                forecasts.add(currCoordFc);
            }
            return new Pair<>(forecasts, coords);
        }
        @Override
        public Pair<List<Forecast.DayForecast>,List<GeolookupData>> getForecastForADay(Date date) {
            List<Forecast.DayForecast> dayForecasts = new ArrayList<>();
            List<GeolookupData> coords = new ArrayList<>();

            Calendar cl = Calendar.getInstance();
            cl.setTime(date);
            int year = cl.get(Calendar.YEAR);
            int dayOfYear = cl.get(Calendar.DAY_OF_YEAR);

            SQLiteDatabase db = getWritableDatabase();

            String selectionClause = ForecastTable.YEAR + " = ? AND " +
                    ForecastTable.DAY_OF_YEAR + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(year), String.valueOf(dayOfYear)};
            Cursor c = db.query(ForecastTable.TABLE_NAME, null, selectionClause, selectionArgs,
                    null, null, null);
            while (c.moveToNext()) {
                double lat = c.getDouble(c.getColumnIndex(ForecastTable.PLACE_LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(ForecastTable.PLACE_LONGITUDE));
                coords.add(new GeolookupData(lat, lon));

                Forecast.DayForecast day  = new Forecast.DayForecast();
                extractDayForecastFromCursor(c, day);
                dayForecasts.add(day);
            }
            return new Pair<List<Forecast.DayForecast>,List<GeolookupData>> (dayForecasts, coords);
        }

        @Override
        public void deleteObsoleteForecasts(Date tresholdDate) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(tresholdDate);
            int year = calendar.get(Calendar.YEAR);
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
            SQLiteDatabase db = getWritableDatabase();
            String whereClause =  ForecastTable.DAY_OF_YEAR +
                    " < ? ";
            String[] whereArgs = new String[] { String.valueOf(dayOfYear)};
            int n = db.delete(ForecastTable.TABLE_NAME, whereClause, whereArgs);
        }

        @Override
        public void deletePlaceForecast(GeolookupData placeCoordinates) {
            SQLiteDatabase db = getWritableDatabase();
            String whereClause = ForecastTable.PLACE_LATITUDE + " = ? AND " +
                    ForecastTable.PLACE_LONGITUDE + " = ? ";
            String[] whereArgs = new String[] { String.valueOf(placeCoordinates.getLat()),
                String.valueOf(placeCoordinates.getLon())};
            int n = db.delete(ForecastTable.TABLE_NAME, whereClause, whereArgs);
        }
    }

}
