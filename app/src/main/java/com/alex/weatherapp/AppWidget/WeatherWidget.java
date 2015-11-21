package com.alex.weatherapp.AppWidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

/**
 * Created by Alex on 04.11.2015.
 */
public class WeatherWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        WeatherWidgetUpdateService.startWidgetUpdate(context);
    }
}
