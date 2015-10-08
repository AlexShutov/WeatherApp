package com.alex;

import android.util.Log;

import com.alex.weatherapp.LoadingSystem.RetrofitLoadingSystem.RetrofitLoadingSystem;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUndergroundGeolookupData;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.IRetrofitGeolookup;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.RetrofitInstanceCreator;

import java.io.IOException;

import retrofit.Call;
import retrofit.Response;

/**
 * Created by Alex on 05.09.2015.
 */
public class GeolookupThread extends Thread {

    public GeolookupThread() {
        super("GeolookupThread");
        mRetrofitCreator = new RetrofitInstanceCreator();
        mIface = mRetrofitCreator.getsGeolookupService();
    }

    @Override
    public void run() {
        Call<WUndergroundGeolookupData> call = mIface.getGeolookupData(RetrofitLoadingSystem.sAppID, 0, 0);
        Response<WUndergroundGeolookupData> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            Log.d("iojad", "IOException is being thrown");
        }
        WUndergroundGeolookupData data = response.body();

    }

    RetrofitInstanceCreator mRetrofitCreator;
    IRetrofitGeolookup mIface;


}
