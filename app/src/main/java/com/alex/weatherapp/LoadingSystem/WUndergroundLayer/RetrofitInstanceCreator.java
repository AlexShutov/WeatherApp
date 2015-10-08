package com.alex.weatherapp.LoadingSystem.WUndergroundLayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Alex on 05.09.2015.
 */
public class RetrofitInstanceCreator {

    public static String sBaseAdress = "http://api.wunderground.com/api";

    private static class DoubleTypeAdapter extends TypeAdapter<Double> {
        @Override
        public Double read(JsonReader reader) throws IOException {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
                return new Double(0);
            }
            String stringValue = reader.nextString();
            return 1.1;
        }

        @Override
        public void write(JsonWriter out, Double value) throws IOException {
            if (value == null){
                out.nullValue();
                return;
            }
            out.value(value);
        }
    }

    IRetrofitGeolookup mGeolookupService;
    OkHttpClient mOkHttpClient;
    Gson mGSON;
    Retrofit mRetrofit;

    public RetrofitInstanceCreator() {
        mGeolookupService = null;
        mOkHttpClient = null;
        mGSON = null;
        mRetrofit = null;
    }

    // lazy init
    public Retrofit initNGetRetrofit() {
        if (mRetrofit != null) {
            return  mRetrofit;
        }
        mOkHttpClient = new OkHttpClient();
        mGSON = new GsonBuilder().registerTypeAdapter(Double.class,
                new DoubleTypeAdapter()).create();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(sBaseAdress)
                .addConverterFactory(GsonConverterFactory.create(mGSON))
                .client(mOkHttpClient)
                .build();

        return mRetrofit;
    }

    public IRetrofitGeolookup getsGeolookupService() {
        Retrofit rf = initNGetRetrofit();
        IRetrofitGeolookup gls = rf.create(IRetrofitGeolookup.class);

        return  gls;
    }



}
