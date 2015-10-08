package com.alex.weatherapp;

import android.util.Log;
import android.widget.Toast;

import com.alex.weatherapp.MVP.IModel;
import com.alex.weatherapp.MVP.IPresenter;
import com.alex.weatherapp.MVP.Model;
import com.alex.weatherapp.MVP.Presenter;
import com.alex.weatherapp.MVP.PresenterBase;

/**
 * Created by Alex on 30.09.2015.
 */


/**
 * WeatherApplication class acts as MVP model.
 */
public class WeatherApplication extends android.app.Application {

    public static final String BACK_STACK_TAG = "weather_app";

    @Override
    public void onCreate() {
        super.onCreate();
        init();
       Log.d("WeatherApplication", "Applicatiion object is created");
    }

    /**
     * Add initialization in here
     */
    void init(){
        mDataModel = new Model(getApplicationContext());
        /**
         * does nothing right now
         */
        mDataModel.initModel();
        mDefaultPresenter = new Presenter();
        mDefaultPresenter.tapIntoModel(mDataModel);
        Log.d("AppClass", "Model is initialized");
    }

    @Override
    public void onTerminate() {
        Log.d("WeatherApplication", "Applicatiion object is terminating");
        super.onTerminate();
    }

    public IModel getDataModel(){ return mDataModel;}
    public IPresenter getDefaultPresenter(){ return  mDefaultPresenter;}

    private IModel mDataModel;
    private IPresenter mDefaultPresenter;
}
