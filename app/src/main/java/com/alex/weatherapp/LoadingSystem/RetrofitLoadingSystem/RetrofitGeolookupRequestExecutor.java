package com.alex.weatherapp.LoadingSystem.RetrofitLoadingSystem;

import android.util.Log;

import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupData;
import com.alex.weatherapp.LoadingSystem.IResponse;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUndergroundGeolookupData;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.IRetrofitGeolookup;
import com.alex.weatherapp.LoadingSystem.BoundRequestExecutorAbstract;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupRequest;
import com.alex.weatherapp.LoadingSystem.GeolookupRequest.GeolookupResponse;
import com.alex.weatherapp.LoadingSystem.ICallback;
import com.alex.weatherapp.LoadingSystem.RequestAbstract;
import com.alex.weatherapp.LoadingSystem.LoadingSystem;
import com.alex.weatherapp.LoadingSystem.RequestExecutorTypeMismatchExceptioin;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Alex on 06.09.2015.
 */



public class RetrofitGeolookupRequestExecutor extends BoundRequestExecutorAbstract {

    public RetrofitGeolookupRequestExecutor() {
        mSystem = null;
        mGeolookupIface = null;
    }

    @Override
    public void bindToLoadingSystem(LoadingSystem sys) throws  IllegalArgumentException{
        if (sys == null)
            throw new  IllegalArgumentException("system reference is nullable");

        if (!(sys instanceof RetrofitLoadingSystem)) {
            throw new IllegalArgumentException("wrong system type, Retrofit is required");
        }

        mSystem = (RetrofitLoadingSystem) sys;
        Retrofit rf = mSystem.getRetrofitRef();
        mGeolookupIface = rf.create(IRetrofitGeolookup.class);
    }

    /**
     *
     * @param request
     * @param callback
     * @throws RequestExecutorTypeMismatchExceptioin RequestExecutorTypeMismatchExceptioin
     * is thrown when request isn't an instance of GeolookupRequest
     * @throws IllegalStateException
     */
    @Override
    public void execute(RequestAbstract request, final ICallback callback) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        if (! (request instanceof GeolookupRequest))
            throw  new RequestExecutorTypeMismatchExceptioin();
        GeolookupRequest glReq = (GeolookupRequest) request;
        if (mGeolookupIface == null) {
            boolean ok = true;
            try {
                mGeolookupIface = mSystem.getRetrofitRef().create(IRetrofitGeolookup.class);
            }catch (Throwable t) {
                ok = false;
            }
            if (!ok || mGeolookupIface == null)
                throw new IllegalStateException("system isn't ready and that can't be fixed, aborting");
        }

        /* acquire 'future' object for async execution */
        double lat = glReq.getLat();
        double lon = glReq.getLon();
        final Call<WUndergroundGeolookupData> call = mGeolookupIface.getGeolookupData(RetrofitLoadingSystem.sAppID,
                lat, lon);

        call.enqueue(new Callback<WUndergroundGeolookupData>() {
            @Override
            public void onResponse(Response<WUndergroundGeolookupData> response) {

                GeolookupResponse res = null;
                if (response.isSuccess()) {
                    WUndergroundGeolookupData data =  response.body();
                    res = new GeolookupResponse(data);
                }
                else {
                    ResponseBody eb = response.errorBody();
                    String msg = response.message();
                    Log.d("request failure", msg);
                    /* init with null, success flag is cleared */
                    res = new GeolookupResponse(null);
                }
                /* notify UI thread */
                callback.onResult(res);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("request failure", t.getMessage());
                // inform about failure
                callback.onResult(new GeolookupResponse(null));
            }
        });
    }

    @Override
    public IResponse execute(RequestAbstract request) throws RequestExecutorTypeMismatchExceptioin, IllegalStateException {
        if (! (request instanceof GeolookupRequest))
            throw  new RequestExecutorTypeMismatchExceptioin();
        GeolookupRequest glReq = (GeolookupRequest) request;
        if (mGeolookupIface == null) {
            boolean ok = true;
            try {
                mGeolookupIface = mSystem.getRetrofitRef().create(IRetrofitGeolookup.class);
            }catch (Throwable t) {
                ok = false;
            }
            if (!ok || mGeolookupIface == null)
                throw new IllegalStateException("system isn't ready and that can't be fixed, aborting");
        }

        /* acquire 'future' object for async execution */
        double lat = glReq.getLat();
        double lon = glReq.getLon();
        Call<WUndergroundGeolookupData> call = mGeolookupIface.getGeolookupData(RetrofitLoadingSystem.sAppID,
                lat, lon);
        GeolookupData data = null;
        try {
            data = call.execute().body();
        } catch (IOException e) {
            throw new IllegalStateException();
        }
        GeolookupResponse result = new GeolookupResponse(data);
        return  result;
    }

    private RetrofitLoadingSystem mSystem;
    private IRetrofitGeolookup mGeolookupIface;
}
