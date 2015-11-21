package com.alex.weatherapp.UI.Test;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.alex.weatherapp.R;
import com.alex.weatherapp.UIv2.TestActivity;

public class RandomService extends Service {
    public static final String ACTION_RANDOM_NUMBER =
            "com.alex.weatherapp.appwidget.ACTION_RANDOM_NUMBER";

    private static int sRandomNumber;
    public static int getsRandomNumber(){
        return sRandomNumber;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sRandomNumber = (int)(100 * Math.random());

        /** Create appwidget view */
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.simple_widget_layout);
        views.setTextViewText(R.id.text_number, String.valueOf(getsRandomNumber()));

        // Set an intent for refresh button to start this service again
        PendingIntent refreshIntent =
                PendingIntent.getService(this, 0, new Intent(this, RandomService.class), 0);
        views.setOnClickPendingIntent(R.id.button_refresh, refreshIntent);

        // Set an Intent so tapping the widget text will open the Activity
        PendingIntent appIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, TestActivity.class), 0);
        views.setOnClickPendingIntent(R.id.containter, appIntent);

        // Update the widget
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName widget = new ComponentName(this, SimpleAppWidget.class);
        manager.updateAppWidget(widget, views);

        // Fire a broadcast to notify listeners
        Intent broadcast = new Intent(ACTION_RANDOM_NUMBER);
        sendBroadcast(broadcast);

        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
