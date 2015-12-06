package com.alex.weatherapp.LocationAPI;

/**
 * Created by Alex on 05.12.2015.
 */

import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;

import com.alex.weatherapp.Utils.Logger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Each LibFeature has a queue of pending tasks being received by GoogleLibFrame and stack here.
 * At a time library is initialized, GoogleLibFrame allows every LibFeature to process its
 * pending tasks (Visitor pattern)
 */
public abstract class LibFeature {

    public LibFeature(GoogleLibFrame frame){
        mPendingRequests = new LinkedList<>();
        mHoldingFrame = frame;
        mIsAPISupportsFeature = false;
        mIsRunning = false;
    }

    /** Tag class, extend in a concrete feature
     * All data inside GoogleLibFrame stored as Intent. Initial request data is packed into that
     * intent and dwell there till the end. !!!Processing entities must not change those
     * data, because at a final stage initial data unpacked again and handed back to user.
     */
    public static class LocationResultData {
        public LocationResultData(){}

    }
    interface IUserLocationCallback{
        void onTaskCompleted(LocationResultData data);
        void onError(String errorMessage);
    }

    public static abstract class RequestBuilder{
        public abstract Intent createRequest();
    }

    /**
     * Compared to RequestBuilder, ResultParser is declared as 'static' Even thought
     * it may want to use some data or state variables from LibFeature, it is dangerous to do,
     * because ResultParser is meant to be used from IntentService on another thread. If Activity'd
     * go out of scope this might lead to memory leak. GoogleLibFrame maintains a set of
     * unique RequestParsers and hands the one which fits to IntentService.
     * parseResult takes necessary action for processing the result and altering Intent state.
     * For example, it may contain a list of critical places, demanding user attention. If received
     * result relate to non-critical place, it may just post non-obtrusive notification about
     * that result, otherwise, it may pass result to GoogleLibFrame, so it can process it
     * further and notify user.
     */
    public abstract static class ServiceResultParser{
        /** Even thought Intent carries ResultReceiver, it is up to Service to
         * extract it and validate its state. Service uses this class only when receiving
         * side is ready for processing a result.
         * @param intent
         */

        public abstract void parseResult(final Intent intent, Context context);
        public void setResultReceiver(ResultReceiver receiver){ resultReceiver = receiver;}
        public ResultReceiver getResultReceiver(){ return resultReceiver;}
        private ResultReceiver resultReceiver;
    }

    /**
     * This class is the last class used in result processing. First- IntentService receives
     * an Intent containing result in format, specific to its request type.
     * Service Uses Service ResultParser
     */
    public abstract class FinalResultProcessor {
        public abstract void processResultInFrame(int resultCode, Intent resultData);
    }

    /**
     * Subclasses will return a covariant request builder
     * @return
     */
    public abstract RequestBuilder createRequestBuilder();

    public abstract FinalResultProcessor createFinalResultProcessor();

    /**
     * GoogleLibFrame will call this method when IntentService will have demand IRequestParser
     * for parsing a responce from GooglePlayLibrary. Result intent carries response type as
     * String Extra.
     * @return
     */
    public abstract ServiceResultParser createResultParser();

    /**
     * This tag serves as a key in Map<String,LibFeature> inside GoogleLibFrame and also allows
     * distinguishing type of request, response, and Feature type.
     * @return
     */
    public abstract String getFeatureDescriptionTag();

    /** Here comes lifecycle callback methods. Idea is that every feature might react differently
     * to lifecycle events.
     */
    public void processRequests(GoogleLibFrame frame){
        if (mPendingRequests.isEmpty() && mIsRunning){
            Logger.d(getFeatureDescriptionTag() + " has no pending tasks ");
            return;
        }
        for (Intent request : mPendingRequests){
            Logger.d("LocationAPI: Processing pended request for: " +
                    GoogleLibFrame.getKindOfFeature(request));
            if (mIsRunning) {
                processRequest(request);
            }else {
                Logger.w("Can't process pending request because API has suddenly became " +
                        "suspended");
                return;
            }
        }
    }

    /** Must be used only be GoogleLibFrame */
    public void addPendingRequest(Intent intent) throws IllegalArgumentException
    {
        if (mIsRunning && !isFeatureSupportedByAPI()){
            Logger.e("API is running, but it doesn't support this kind of feature: " +
                    getFeatureDescriptionTag());
            return;
        }
        if (null == intent ||
                !GoogleLibFrame.getKindOfFeature(intent).equals(getFeatureDescriptionTag())){
            throw new IllegalArgumentException("Request data is corrupted");
        }
        /** this branch might be reached when API gets suspended and right after that many
         * tasks is queued. But very soon API bring up again, so those tasks must be processed
         * right away.
         */
        if (mIsRunning){
            processRequest(intent);
            return;
        }
        mPendingRequests.add(intent);
    }

    public boolean isHavingPendingRequests(){ return !mPendingRequests.isEmpty(); }

    /**
     * Process data for this kind of feature here. For example, for inverse geolookup, request
     * Geocoder to give you place name. This method is called directly (avoiding queue) by
     * GooleLibFrame, only if frame is ready and this feature has no pending requests
     * @param intent
     */
    public abstract void processRequest(Intent intent);

    /** Check whether functionality specific to this feature is availible and ready
     * for example if(Geocoder.isPresented())
     * @return
     */

    public abstract boolean validateAPIFeatureReady(GoogleApiClient apiClient);

    /** Now we can process all pending requests */
    public void onAPIConnected(){
        mIsAPISupportsFeature = validateAPIFeatureReady(mHoldingFrame.getApiClient());
        if (!mIsAPISupportsFeature){
            Logger.e("Requested feature isn't supported by API, aborting execution");
            return;
        }
        mIsRunning = true;
        processRequests(mHoldingFrame);
    }
    public void onAPISuspended(){
        mIsRunning = false;
    }
    public void onConnectionFailed(ConnectionResult result){
        /** mIsRunning now is false anyway, just to make sure */
        mIsRunning = false;
    }

    protected GoogleLibFrame getFrame(){return mHoldingFrame; }
    public boolean isFeatureSupportedByAPI(){ return mIsAPISupportsFeature; }

    private boolean mIsRunning;
    private boolean mIsAPISupportsFeature;
    private Queue<Intent> mPendingRequests;
    private GoogleLibFrame mHoldingFrame;
}
