package com.alex.weatherapp.AppWidget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.alex.weatherapp.LoadingSystem.ForecastRequest.Forecast;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.LocationData;
import com.alex.weatherapp.LoadingSystem.PlaceForecast;
import com.alex.weatherapp.R;


public class WeatherWidgetUpdateService extends IntentService {
    public static final String ACTION_WIDGET_UPDATING =
            "com.alex.weatherapp.appwidget.ACTION_WIDGET_UPDATING";

    /**
     * Static wrapper for launching this service from receiver
     * @param context
     */
    public static void startWidgetUpdate(Context context){
        Intent startIntent = new Intent(context, WeatherWidgetUpdateService.class);
        context.startService(startIntent);
    }

    public WeatherWidgetUpdateService() {
        super("WeatherWidgetUpdateService");
        mUIThreadHandler = new Handler(Looper.getMainLooper());
        mPlaceForecast = null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Forecast f = new Forecast();
        Forecast.DayForecast df = new Forecast.DayForecast();
        df.tempLow = -10;
        df.tempHigh = +40;
        df.conditions = "OK";
        f.mDayForecasts.add(df);
        mPlaceForecast = new PlaceForecast(new LocationData(0, 0, "Taganrog"), f);
        final PlaceForecast pf = mPlaceForecast;
        if (mPlaceForecast != null) {
            mUIThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateWidget(pf);
                }
            });
            showMessage("Update method is called");
        }
    }

    private void updateWidget(PlaceForecast forecast){
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.weather_widget_layout);
        PendingIntent refreshIntent =
                PendingIntent.getService(this, 0, new Intent(this, WeatherWidgetUpdateService.class), 0);
        views.setOnClickPendingIntent(R.id.idc_wd_btn_refresh, refreshIntent);

        LocationData place = forecast.getPlace();
        views.setTextViewText(R.id.idc_wd_place_name, place.getmPlaceName());

        Forecast.DayForecast dayForecast = null;
        if (!forecast.getForecast().mDayForecasts.isEmpty()){
            dayForecast = forecast.getForecast().mDayForecasts.get(0);
        }
        String strTempFrom = "From: " + String.format("%.2f", dayForecast.tempLow);
        views.setTextViewText(R.id.idc_wd_text_from, strTempFrom);
        String strTempTo = " to: " + String.format("%.2f", dayForecast.tempHigh);
        views.setTextViewText(R.id.idc_wd_text_to, strTempTo);
        views.setTextViewText(R.id.idc_wd_text_details, dayForecast.conditions);

        /** Update the widget */
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName widget = new ComponentName(this, WeatherWidget.class);
        manager.updateAppWidget(widget, views);

        /** Fire a broadcast to notify listeners */
        Intent broadcast = new Intent(ACTION_WIDGET_UPDATING);
        sendBroadcast(broadcast);
    }

    private void showMessage(final String msg){
        mUIThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WeatherWidgetUpdateService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private PlaceForecast mPlaceForecast;
    private Handler mUIThreadHandler;
}
