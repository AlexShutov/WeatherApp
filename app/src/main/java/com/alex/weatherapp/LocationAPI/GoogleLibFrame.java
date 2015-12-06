package com.alex.weatherapp.LocationAPI;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Map;
import java.util.TreeMap;




/**
 * Created by Alex on 05.12.2015.
 */
public class GoogleLibFrame implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String KEY_FEATURE_KIND = "kind_of_feature";
    public static final String KEY_FINAL_RESULT_RECEIVER = "final_result_receiver";
    public static final int RESULT_OK = 0;
    public static final int RESULT_ERROR = 1;

    public static String getKindOfFeature(Intent intent) throws IllegalArgumentException{
        if (!intent.hasExtra(KEY_FEATURE_KIND)){
            throw new IllegalArgumentException("intent has no information about feature kind");
        }
        return intent.getStringExtra(KEY_FEATURE_KIND);
    }
    public static void setFeatureKind(Intent intent, String featureKind){
        intent.putExtra(KEY_FEATURE_KIND, featureKind);
    }
    public static ResultReceiver extractResultReceiver(Intent intent) throws IllegalArgumentException{
        if (!intent.hasExtra(KEY_FINAL_RESULT_RECEIVER)){
            throw new IllegalArgumentException("LocationAPI: Intent has no ResultReceiver");
        }
        ResultReceiver resultReceiver = intent.getParcelableExtra(KEY_FINAL_RESULT_RECEIVER);
        return resultReceiver;
    }
    /**
     * The Service, which processes results doesn't know about GoogleLibFrame instance.
     * Another solution- define ServiceResultParser as Parcelable and pass it in Intent
     * @param featureTag
     * @return
     */
    public static LibFeature.ServiceResultParser createServiceResultParser(String featureTag)
        throws IllegalArgumentException{
        switch (featureTag){
            case DummyFeature.FEATURE_NAME:
                return new DummyFeature.DummyFeatureServiceResultParser();
            default:
                throw new IllegalArgumentException("There are no feature: " + featureTag);
        }
    }

    public class FrameResultReceiver extends ResultReceiver {
        /** we can instantiate it only from GoogleLibFrame */
        private FrameResultReceiver() {
            super(new Handler());
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Intent intent = new Intent();
            intent.putExtras(resultData);
            String kinfOfFeature = null;
            try {
                kinfOfFeature = getKindOfFeature(intent);
            }catch (IllegalArgumentException e){
                Logger.e("LocationAPI: ServiceResult doesn't have info about kind of feature");
                return;
            }
            LibFeature feature = getFeature(kinfOfFeature);
            LibFeature.FinalResultProcessor frp = feature.createFinalResultProcessor();
            frp.processResultInFrame(resultCode, intent);
        }
    }

    public GoogleLibFrame(Context context){
        mContext = context;
        mFeatures = new TreeMap<>();
        mFinalResultReceiver = new FrameResultReceiver();
        initFeatures();
        mIsReady = false;
        /** We build client on instance creation but connection
         * is performed in lifecycle methods
         */
        buildApiClient();
    }

    /**
     * Add all supported features here
     */
    private void initFeatures(){
        DummyFeature dummyFeature = new DummyFeature(this);
        mFeatures.put(dummyFeature.getFeatureDescriptionTag(), dummyFeature);
    }
    private LibFeature getFeature(String featureTag) throws IllegalArgumentException{
        if (!mFeatures.containsKey(featureTag)){
            String msg = "Trying to get an not existing feature";
            Logger.e(msg);
            throw new IllegalArgumentException(msg);
        }
        return mFeatures.get(featureTag);
    }


    private void onPlayServiceInitialized(){
        for (Map.Entry<String, LibFeature> sf : mFeatures.entrySet()){
            LibFeature feature = sf.getValue();
            feature.processRequests(this);
        }
    }
    /**
     * In order to add a new request, you first need to create RequestBuilder, and after
     * that pass resulting FeatureData to processRequest method.
     * @param featureName
     * @return
     * @throws IllegalArgumentException
     */
    public LibFeature.RequestBuilder createRequestBuilder(String featureName) throws
        IllegalArgumentException{
        if (!mFeatures.containsKey(featureName)) {
            String msg = "This frame has no such feature, aborting";
            Logger.e(msg);
            throw new IllegalArgumentException(msg);
        }
        LibFeature feature = mFeatures.get(featureName);
        /** you can now cast it to approptiate type */
        LibFeature.RequestBuilder builder = feature.createRequestBuilder();
        return builder;
    }

    public void processRequest(Intent intent){
        LibFeature feature = getFeature(getKindOfFeature(intent));
        if (isReady()){
            feature.processRequest(intent);
        }else {
            feature.addPendingRequest(intent);
        }
    }

    /**
     * Creates GoogleAPI client suited for working with LocationServices. Right not this frame
     * supports only LocationServices, but it can be modified for supporting many different APIs,
     * storing them and accessing by API type.
     * It is synchronized, because fast Activity recreation might lead to the race condition.
     */
    protected synchronized void buildApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onStart(){
        mGoogleApiClient.connect();
    }
    public void onStop(){
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    /** Service may be started explicitly for some kind of features (e.g. inverse geolookup)
     * Method is called from LibFeature.processRequest(..) method when API is up and running
     * and feature need to do some work. Data Intent doesn't carry reference to current
     * FrameResult receiver and RequestBuilder doesn't know anything about it, because it is
     * GoogleLibFrame's responsibility, so we need to include it into request Intent
     * * @param data
     */
    public void bringUpService(Intent data){
        try {
            getKindOfFeature(data);
        }catch (IllegalArgumentException e){
            Logger.e(e.getMessage());
            return;
        }
        data.putExtra(KEY_FINAL_RESULT_RECEIVER, mFinalResultReceiver);
        data.setClass(mContext, GoogleLibFrameIntentService.class);
        mContext.startService(data);
    }

    /**
     * Some features demand API to execute a task, which completion time is
     * unknown. For example, geofencing. Other features just need to make a blocking call
     * from API. The former ones pass PendingIntent, calling out Service. They must reuse the
     * same Intent, stored in GoogleLibFrame.
     * @return
     */
    public PendingIntent getServicePendingIntent(){
        /** TODO, now I only need inverse geolocation, it doesn't need this method */
        return null;
    }

    /** Inherited from Google Api callbacks */
    @Override
    public void onConnected(Bundle bundle) {

        Logger.i("Google Location API is connected");
        for (String featureName : mFeatures.keySet()){
            LibFeature feature = mFeatures.get(featureName);
            feature.onAPIConnected();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        Logger.i("Google Location API were suspended");
        for (String featureName : mFeatures.keySet()){
            LibFeature feature = mFeatures.get(featureName);
            feature.onAPISuspended();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.e("Failed to connect to Google Location API, reason: " +
                connectionResult.getErrorMessage());
        for (String featureName : mFeatures.keySet()){
            LibFeature feature = mFeatures.get(featureName);
            feature.onConnectionFailed(connectionResult);
        }
    }


    public boolean isReady(){ return this.mIsReady;}
    public GoogleApiClient getApiClient(){ return mGoogleApiClient;}
    public Context getContext(){ return mContext;}

    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private boolean mIsReady;
    private FrameResultReceiver mFinalResultReceiver;
    private Map<String, LibFeature> mFeatures;
}
