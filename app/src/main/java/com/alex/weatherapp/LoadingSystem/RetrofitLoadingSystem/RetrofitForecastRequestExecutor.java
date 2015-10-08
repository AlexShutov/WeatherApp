package com.alex.weatherapp.LoadingSystem.RetrofitLoadingSystem;

import android.util.Log;

import com.alex.weatherapp.LoadingSystem.BoundRequestExecutorAbstract;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastRequest;
import com.alex.weatherapp.LoadingSystem.ForecastRequest.ForecastResponse;
import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.LoadingSystem;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;
import com.alex.weatherapp.LoadingSystem.RequestExecutorTypeMismatchExceptioin;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.IRetrofitForecast;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUForecastData;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Alex on 06.09.2015.
 */
public class RetrofitForecastRequestExecutor extends BoundRequestExecutorAbstract {


    public RetrofitForecastRequestExecutor() {
        mSystem = null;
        mForecastIface = null;
    }

    @Override
    public void bindToLoadingSystem(LoadingSystem sys) throws IllegalArgumentException {
        if (sys == null)
            throw new  IllegalArgumentException("system reference is nullable");

        if (!(sys instanceof RetrofitLoadingSystem)) {
            throw new IllegalArgumentException("wrong system type, Retrofit is required");
        }

        mSystem = (RetrofitLoadingSystem) sys;
        Retrofit rf = mSystem.getRetrofitRef();
        mForecastIface = rf.create(IRetrofitForecast.class);

    }

    /**
     *
     * @param request ForecastRequest to be handled
     * @param callback
     * @throws RequestExecutorTypeMismatchExceptioin is thrown when request isn't an instance of
     * ForecastRequest
     * @throws IllegalStateException
     * It is possible to make a ganaric executor, cause handling is almost the same,
     * but there is just two executors, so it doesn't worth the efforts
     */
    @Override
    public void execute(RequestAbstract request, final ICallback callback)
            throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        if (! (request instanceof ForecastRequest))
            throw  new RequestExecutorTypeMismatchExceptioin();
        ForecastRequest forecastRequest = (ForecastRequest) request;
        if (mForecastIface == null) {
            boolean ok = true;
            try {
                mForecastIface = mSystem.getRetrofitRef().create(IRetrofitForecast.class);
            }catch (Throwable t) {
                ok = false;
            }
            if (!ok || mForecastIface == null)
                throw new IllegalStateException("system isn't ready and that can't be fixed, aborting");
        }

        double lat = forecastRequest.getLat();
        double lon = forecastRequest.getLon();
        String forecastTypeWord = "";
        switch (forecastRequest.getmForecastType()) {
            case Forecast_3Days:
                forecastTypeWord = "forecast";
                break;
            case Forecast_10Days:
                forecastTypeWord = "forecast10day";
                break;
            default:
                forecastTypeWord = "forecast";
        }
        final Call<WUForecastData> call =
                mForecastIface.getForecast(RetrofitLoadingSystem.sAppID, forecastTypeWord, lat, lon);
        call.enqueue(new Callback<WUForecastData>() {
            @Override
            public void onResponse(Response<WUForecastData> response) {

                ForecastResponse res = null;
                if (response.isSuccess()) {
                    WUForecastData data =  response.body();
                    res = new ForecastResponse(data);
                }
                else {
                    ResponseBody eb = response.errorBody();
                    String msg = response.message();
                    Log.d("request failure", msg);
                    /* init with null, success flag is cleared */
                    res = new ForecastResponse(null);
                }
                /* notify UI thread */
                callback.onResult(res);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("request failure", t.getMessage());
                // inform about failure
                callback.onResult(new ForecastResponse(null));
            }
        });

    }

    @Override
    public IResponse execute(RequestAbstract request) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        if (! (request instanceof ForecastRequest))
            throw  new RequestExecutorTypeMismatchExceptioin();
        ForecastRequest forecastRequest = (ForecastRequest) request;
        if (mForecastIface == null) {
            boolean ok = true;
            try {
                mForecastIface = mSystem.getRetrofitRef().create(IRetrofitForecast.class);
            }catch (Throwable t) {
                ok = false;
            }
            if (!ok || mForecastIface == null)
                throw new IllegalStateException("system isn't ready and that can't be fixed, aborting");
        }

        double lat = forecastRequest.getLat();
        double lon = forecastRequest.getLon();

        String forecastTypeWord = "";
        switch (forecastRequest.getmForecastType()) {
            case Forecast_3Days:
                forecastTypeWord = "forecast";
                break;
            case Forecast_10Days:
                forecastTypeWord = "forecast10day";
                break;
            default:
                forecastTypeWord = "forecast";
        }
        Call<WUForecastData> call = null;
        try {
            call = mForecastIface.getForecast(RetrofitLoadingSystem.sAppID, forecastTypeWord, lat, lon);
        } catch (Exception e) {
            Log.d("Exception", "GSON parsing exception, returning empty result");
            WUForecastData resData = new WUForecastData();
            return new ForecastResponse(resData);
        }
        WUForecastData resData = null;
        try {
            resData = call.execute().body();
        } catch (IOException e) {
            throw new IllegalStateException("Execution error: " + e.getMessage());
        } catch (Exception nfe) {
            /* Exception may be thrown only for very remote locations, where some parameters is not
             TODO:  write custom Double type converter, to handle this case seamlessly */
            Log.d("Exception:", "Data parsing exception");
            return new ForecastResponse(new WUForecastData());
        }
        ForecastResponse resp = new ForecastResponse(resData);
        return  resp;
    }

    private RetrofitLoadingSystem mSystem;
    IRetrofitForecast mForecastIface;
}
