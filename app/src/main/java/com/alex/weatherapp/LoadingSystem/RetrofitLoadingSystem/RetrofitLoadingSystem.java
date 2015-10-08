package com.alex.weatherapp.LoadingSystem.RetrofitLoadingSystem;

import com.alex.weatherapp.LoadingSystem.LoadingSystem;
import com.alex.weatherapp.LoadingSystem.RequestExecutorDecorator;
import com.alex.weatherapp.LoadingSystem.WUndergroundLayer.WUForecastAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Alex on 05.09.2015.
 */

/* It is just a facade for a components used by Retrofit instance,  passes its reference
 * to executors during their creation in order to instantiate concrete request interface and
  * store it in an executor. Also supports protocols timeouts customiation */

public class RetrofitLoadingSystem extends LoadingSystem {

    public static final String sAppID = "aff2e92e95e8676c";
    /* string for a particular service, perhaps it would be better to move it in a subclass */
    protected static String sBaseAdress = "http://api.wunderground.com/api";

    private static final int TIMEOUT = 60;
    private static final int WRITE_TIMEOUT = 120;
    private static final int CONNECT_TIMEOUT = 10;

    public RetrofitLoadingSystem() {
        super.init();
        resetComponentsLinks();
        createNLinkUpComponents();

        addAllSupportedExecutors();
    }

    /* Assing nulls to avoid runtime errors due to
     linking up unexisting components */
    protected  void resetComponentsLinks() {
        mOkKttpClient = null;
        mGSON = null;
        mRetrofit = null;
    }

    @Override
    protected void addAllSupportedExecutors() {
        /* Create and register geolookup executor  */
        RetrofitGeolookupRequestExecutor geolookupExec = new RetrofitGeolookupRequestExecutor();
        geolookupExec.bindToLoadingSystem(this);
        addExecutor(geolookupExec);

        /* Create and register forecast executor, create decorator for result
        transformation and register that executor
         */
        RetrofitForecastRequestExecutor forecastExec = new RetrofitForecastRequestExecutor();
        forecastExec.bindToLoadingSystem(this);
        RequestExecutorDecorator forecastWUTransformer = new RequestExecutorDecorator();
        forecastWUTransformer.setExecutor(forecastExec);
        forecastWUTransformer.setDecorator(new WUForecastAdapter());
        addExecutor(forecastWUTransformer);
    }

    /* moved out from init() because compomemts must be recreated and connected after some param is
         * changed */
    protected  void createNLinkUpComponents() {

        mOkKttpClient = new OkHttpClient();
        mOkKttpClient.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        mOkKttpClient.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        mOkKttpClient.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
        mGSON = new GsonBuilder().create();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(sBaseAdress)
                .addConverterFactory(GsonConverterFactory.create(mGSON))
                .client(mOkKttpClient)
                .build();
    }

    /* Accessors  */

    public Retrofit getRetrofitRef() throws  IllegalStateException {
        if (mRetrofit == null) {
            throw new IllegalStateException("Retrofit isn't initialized, aborting");
        }
        return  mRetrofit;
    }

    /* Some executors might want to change client params */
    public OkHttpClient getHttpClient() throws  IllegalStateException {
        if (mOkKttpClient == null) {
            throw new IllegalStateException("error, HTTP client isn't ready");
        }
        return  mOkKttpClient;
    }

    OkHttpClient mOkKttpClient;
    Gson mGSON;
    Retrofit mRetrofit;


}
